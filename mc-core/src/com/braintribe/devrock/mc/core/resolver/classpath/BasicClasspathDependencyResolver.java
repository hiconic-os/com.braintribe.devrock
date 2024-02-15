// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.devrock.mc.core.resolver.classpath;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.download.PartEnricher;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.download.PartEnrichingContextBuilder;
import com.braintribe.devrock.mc.api.transitive.ArtifactPathElement;
import com.braintribe.devrock.mc.api.transitive.DependencyPathElement;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContextBuilder;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.resolver.clashes.ClashResolver;
import com.braintribe.devrock.mc.core.resolver.common.AnalysisArtifactResolutionPreparation;
import com.braintribe.devrock.model.mc.reason.ClasspathInvalidDependencyReference;
import com.braintribe.devrock.model.mc.reason.ClasspathInvalidPartReference;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.analysis.DependencyClash;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.PartEnrichment;
import com.braintribe.model.artifact.essential.PartIdentification;

public class BasicClasspathDependencyResolver implements ClasspathDependencyResolver {
	
	private static final String SCOPE_TEST = "test";
	private static final String SCOPE_RUNTIME = "runtime";
	private static final String SCOPE_COMPILE = "compile";
	private static final String SCOPE_PROVIDED = "provided";
	
	private static final Set<String> transitiveCompileScopes = new HashSet<>(Arrays.asList(SCOPE_COMPILE, SCOPE_PROVIDED));
	private static final Set<String> transitiveRuntimeScopes = new HashSet<>(Arrays.asList(SCOPE_COMPILE, SCOPE_RUNTIME));
	private static final Set<String> transitiveTestScopes = new HashSet<>(Arrays.asList(SCOPE_COMPILE, SCOPE_PROVIDED, SCOPE_TEST, SCOPE_RUNTIME));
	
	private static final Comparator<AnalysisArtifact> solutionSortingComparator = Comparator.comparing(AnalysisArtifact::getArtifactId).thenComparing(Comparator.comparing(AnalysisArtifact::getGroupId));
	
	private TransitiveDependencyResolver transitiveDependencyResolver;
	
	private PartEnricher partEnricher;
	
	@Configurable
	@Required
	public void setPartEnricher(PartEnricher partEnricher) {
		this.partEnricher = partEnricher;
	}
	
	@Configurable
	@Required
	public void setTransitiveDependencyResolver(TransitiveDependencyResolver transitiveDependencyResolver) {
		this.transitiveDependencyResolver = transitiveDependencyResolver;
	}

	@Override
	public AnalysisArtifactResolution resolve(ClasspathResolutionContext context, Iterable<? extends CompiledTerminal> terminals) {
		requireNonNull(context, "Cannot resolve classpath as resolution context is null");
		return new StatefulClasspathResolver(context, terminals).resolve();
	}
	
	/**
	 * stateful (and synchronous) classpath resolver : the actual resolving logic
	 * @author pit / dirk
	 *
	 */
	private class StatefulClasspathResolver {
		private List<Pair<AnalysisArtifact, Reason>> collectedErrors = new ArrayList<>();
		private ClasspathResolutionContext context;
		private Iterable<? extends CompiledTerminal> terminals;
		private Set<EqProxy<CompiledDependency>> providedDependencies = ConcurrentHashMap.newKeySet();
		private ClasspathResolutionScope scope;

		public StatefulClasspathResolver(ClasspathResolutionContext context, Iterable<? extends CompiledTerminal> terminals) {
			this.context = context;
			this.terminals = terminals;
			this.scope = context.scope();
		}

		public AnalysisArtifactResolution resolve() {
			TransitiveResolutionContextBuilder transitiveResolutionContextBuilder = TransitiveResolutionContext.build() // 
				.artifactTransitivityPredicate(this::isTransitiveArtifact) //
				.artifactPathFilter(this::filterArtifact) // internal artifact path filter 
				.dependencyPathFilter(this::filterDependencyPath) // internal dependency path filter
				.customScopeSuppplier(this::extractCustomScope) //
				.globalExclusions( this.context.globalExclusions()) //
				.lenient(true) //
				.respectExclusions(true);
			

			// if a artifact path filter's additionally set from the context, add it (TRC builder will create a conjunction filter) 
			Predicate<? super ArtifactPathElement> artifactPathFilter = context.artifactPathFilter();
			if (artifactPathFilter != null) {
				transitiveResolutionContextBuilder.artifactPathFilter(artifactPathFilter);
			}
			
			// if a dependency path filter's additionally set from the context, add it (TRC builder will create a conjunction filter)
			Predicate<? super DependencyPathElement> dependencyPathFilter = context.dependencyPathFilter();
			if (dependencyPathFilter != null) {
				transitiveResolutionContextBuilder.dependencyPathFilter( dependencyPathFilter);
			}
			
				
			TransitiveResolutionContext transitiveResolutionContext = transitiveResolutionContextBuilder.done(); //
			
			// get all involved artifacts from the transitive dependency resolver 
			AnalysisArtifactResolution resolution = transitiveDependencyResolver.resolve(transitiveResolutionContext, terminals);
			
			List<AnalysisTerminal> analysisTerminals = resolution.getTerminals();
			List<AnalysisDependency> dependencies = new ArrayList<>();
			
			for (AnalysisTerminal terminal: analysisTerminals) {
				if (terminal instanceof AnalysisArtifact) {
					AnalysisArtifact artifact = (AnalysisArtifact)terminal;
					dependencies.addAll(artifact.getDependencies());
				}
				else if (terminal instanceof AnalysisDependency) {
					AnalysisDependency dependency = (AnalysisDependency)terminal;
					dependencies.add(dependency);
				}
			}
			

			// resolve clashes
			// TODO : review
			Maybe<List<DependencyClash>> dependencyClashesMaybe = ClashResolver.resolve(dependencies, context.clashResolvingStrategy());
			if (dependencyClashesMaybe.isSatisfied()) {
				resolution.setClashes(dependencyClashesMaybe.get());		
			}
			else {
				if (dependencyClashesMaybe.hasValue()) {
					resolution.setClashes(dependencyClashesMaybe.value());				
				}
				AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolution).getReasons().add( dependencyClashesMaybe.whyUnsatisfied());
				return resolution;
			}		
			
			// analyze clash free tree for valid artifact relations and classpath contributions  
			AnalysisArtifactResolutionPreparation analysisArtifactResolutionPreparation = new AnalysisArtifactResolutionPreparation(resolution, solutionSortingComparator, this::filterSolution);
			analysisArtifactResolutionPreparation.process();
			
			// enrich required parts on all artifacts remaining
			enrich(resolution);
			
			List<Reason> collectedReasons = new ArrayList<>();
			// make sure we do not duplicate the incomplete artifacts .. 
			Set<AnalysisArtifact> collectedArtifacts = new HashSet<>( resolution.getIncompleteArtifacts());
			resolution.getIncompleteArtifacts().clear();
			
			// retrieve errors collected during processing
			if (!collectedErrors.isEmpty()) {				
				collectedErrors.stream().filter( p -> p != null).forEach( p -> {					
					Reason reason= p.second();
					collectedReasons.add( reason);
					collectedArtifacts.add( p.first);
					// TODO: use reasons instead of exception where ever possible and link reason to AnalysisArtifact
				});
				AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolution).getReasons().addAll(collectedReasons); 
			}
			resolution.getIncompleteArtifacts().addAll(collectedArtifacts);
			

			// check whether to throw an exception 
			if (!context.lenient() && resolution.getFailure() != null) {
				throw new IllegalStateException(resolution.getFailure().stringify());
			}
				
			return resolution;
		}
		
		private boolean filterSolution(AnalysisArtifact solution) {
			return !"pom".equals(solution.getOrigin().getPackaging());
		}

		private PartEnrichment buildJarAttachmentEnrichment(String classifier) {
			PartEnrichment enrichment = PartEnrichment.T.create();
			enrichment.setClassifier(classifier);
			enrichment.setType("jar");
			enrichment.setKey(classifier);
			return enrichment;
		}
		
		private PartEnrichment buildJarEnrichment(String classifier) {
			PartEnrichment enrichment = PartEnrichment.T.create();
			enrichment.setClassifier(classifier);
			enrichment.setType("jar");
			enrichment.setMandatory(true);
			return enrichment;
		}
		
		private boolean isJarDependency(AnalysisDependency dependency) {
			String type = dependency.getType();
			
			if (type == null)
				type = "jar";
			
			return type.equals("jar");
		}
		
		private Collection<String> extractJarClassifiers(AnalysisArtifact artifact) {
			if ("pom".equals(artifact.getOrigin().getPackaging()))
				return Collections.emptyList();
			
			Set<AnalysisDependency> dependers = artifact.getDependers();
			
			if (dependers.size() == 0)
				return Collections.emptyList();
			
			return dependers.stream() //
					.filter(this::isJarDependency) //
					.map(AnalysisDependency::getClassifier) //
					.collect(Collectors.toSet()); 
		}
		
		private List<PartEnrichment> determineJarEnrichment(AnalysisArtifact artifact) {
			
			Collection<String> jarClassifiers = extractJarClassifiers(artifact);
			
			if (jarClassifiers.isEmpty())
				return Collections.emptyList();

			List<PartEnrichment> partEnrichments = new LinkedList<>();
			
			if (context.enrichJar()) {
				jarClassifiers.stream().map(this::buildJarEnrichment).forEach(partEnrichments::add);
			}
			
			if (context.enrichSources()) {
				partEnrichments.add(buildJarAttachmentEnrichment("sources"));
			}
			
			if (context.enrichJavadoc()) {
				partEnrichments.add(buildJarAttachmentEnrichment("javadoc"));
			}
			
			return partEnrichments;
		}
		
		private void enrich(AnalysisArtifactResolution resolution) {
			
			boolean expressiveEnrichment = context.enrichJar() || context.enrichJavadoc() || context.enrichSources();
			PartEnrichingContext additionalEnrichmentContext = context.enrich(); 
			
			if (expressiveEnrichment || additionalEnrichmentContext != null) {
				PartEnrichingContextBuilder contextBuilder = PartEnrichingContext.build();
				
				if (expressiveEnrichment) 
					contextBuilder.enrichingExpert(this::determineJarEnrichment);
				
				if (additionalEnrichmentContext != null)
					contextBuilder.enrichingExpert(additionalEnrichmentContext.enrichmentExpert());

				PartEnrichingContext enrichingContext = contextBuilder.done();
				
				partEnricher.enrich(enrichingContext, resolution);
			}
		}
		
		private boolean isTerminalDependency(DependencyPathElement dependencyPathElement) {
			ArtifactPathElement parent = dependencyPathElement.getParent();
			return parent == null || parent.getParent() == null;
		}
		
		public boolean filterDependencyPath(DependencyPathElement dependencyPathElement) {
			AnalysisDependency dependency = dependencyPathElement.getDependency();
			
			if (!context.dependencyFilter().test(dependency)) 
				return false;
			
			CompiledDependency compiledDependency = dependency.getOrigin();
			String dependencyScope = compiledDependency.getScope();
			
			boolean isTerminalDependency = isTerminalDependency(dependencyPathElement);

			switch (scope) {
			case compile:
				// filter non-transitive
				if (!isTerminalDependency && (compiledDependency.getOptional() || dependencyScope.equals(SCOPE_PROVIDED)))
					return false;
				
				// filter by scope
				if (!transitiveCompileScopes.contains(dependencyScope))
					return false;
				
				break;
				
			// TODO : review changes in logic 
			case test:
				// filter non-transitive
				// TODO: check if provided is also relevant for terminal test artifacts
				if ((!isTerminalDependency && ( (compiledDependency.getOptional() || dependencyScope.equals(SCOPE_TEST))) || dependencyScope.equals(SCOPE_PROVIDED) ))
					return false;
				
				// filter by scope
				if (!transitiveTestScopes.contains(dependencyScope))
					return false;
				
				break;
				
			case runtime:
				// filter terminal provided dependencies and mark them for further filtering
				if (isTerminalDependency && dependencyScope.equals(SCOPE_PROVIDED)) {
					// mark provided dep for transitive filtering
					providedDependencies.add(HashComparators.scopelessCompiledDependency.eqProxy(compiledDependency));
					return false;
				}
				
				// filter dependencies if the where marked as provided by the terminal
				if (providedDependencies.contains(HashComparators.scopelessCompiledDependency.eqProxy(compiledDependency))) {
					return false;
				}
				
				// filter optionals
				if (compiledDependency.getOptional())
					return false;
				
				// filter by scope
				if (!transitiveRuntimeScopes.contains(dependencyScope))
					return false;
				break;
				
			default:
				break;
			
			}
			
			String type = compiledDependency.getType();
			
			if (type == null)
				return true;
			
			switch (type) {
			case "jar":
			case "pom":
			case "bundle":
				return true;
			default:
				return false;
			}
		}
		
		/**
		 * @param artifactElement
		 * @return
		 */
		public boolean filterArtifact(ArtifactPathElement artifactElement) {
			
			DependencyPathElement parent = artifactElement.getParent();
			// no parent -> must be terminal, and these we can't filter
			if (parent == null) {
				return true;
			}
			
			// otherwise, get how this artifact is referenced by it's predecessor in the path
			PartIdentification dependencyPartIdentification = parent.getDependency();
			
			/*
			PartIdentification dependencyPartIdentification = Optional.ofNullable(artifactElement.getParent()) //
					.map(e -> (PartIdentification)e.getDependency()) //
					.orElse(PartIdentifications.jar);
			*/
			
			AnalysisArtifact artifact = artifactElement.getArtifact();
			
			ArtifactPathElement owningArtifactPathElement = artifactElement.getParent() != null ? artifactElement.getParent().getParent() : null;
			AnalysisArtifact owningArtifact = owningArtifactPathElement != null ? owningArtifactPathElement.getArtifact() : artifact;
						
			
			String packaging = artifact.getOrigin().getPackaging();

			String dependencyType = dependencyPartIdentification.getType();

			if (dependencyType == null)
				dependencyType = packaging;

			
			switch (packaging) {
			case "war":
			case "ear":
				if (HashComparators.partIdentification.compare(PartIdentifications.classes_jar, dependencyPartIdentification)) {
					return true;
				}
				else {
					Reason reason = TemplateReasons.build(ClasspathInvalidPartReference.T) //
											.assign( ClasspathInvalidPartReference::setPackaging, packaging) //
											.assign( ClasspathInvalidPartReference::setPathInResolution, artifactElement.asPathString())//
											.toReason();
					collectError(owningArtifact, reason);
					return false;
				}
				
			case "jar":
			case "bundle":
				switch (dependencyType) {
				case "bundle":
				case "jar":
					return true;
				
				default:
					Reason reason = TemplateReasons.build(ClasspathInvalidDependencyReference.T)
										.assign( ClasspathInvalidDependencyReference::setDependencyType, dependencyType) //
										.assign( ClasspathInvalidDependencyReference::setPackaging, packaging) //
										.assign( ClasspathInvalidDependencyReference::setPathInResolution, artifactElement.asPathString()) //					
										.toReason();
					collectError(owningArtifact, reason);
					return false;
				}
				
			case "pom":
				switch (dependencyType) {
				case "pom":				
					return true;
				default:
					Reason reason = TemplateReasons.build(ClasspathInvalidDependencyReference.T)
						.assign( ClasspathInvalidDependencyReference::setDependencyType, dependencyType) //
						.assign( ClasspathInvalidDependencyReference::setPackaging, packaging) //
						.assign( ClasspathInvalidDependencyReference::setPathInResolution, artifactElement.asPathString()) //					
						.toReason();
					collectError(owningArtifact, reason);
					return false;
				}
				
			default:
				return true;
			}
		}
		
		private void collectError(AnalysisArtifact artifact, Reason reason) {
			collectedErrors.add(Pair.of( artifact, reason));
		}
		
		private void collectError(AnalysisArtifact artifact, Exception e) {
			collectError(artifact, InternalError.from(e));
		}
		
		public boolean isTransitiveArtifact(ArtifactPathElement artifactPathElement) {
			DependencyPathElement dependerDependencyElement = artifactPathElement.getParent();
			
			if (dependerDependencyElement == null)
				return true;
			
			AnalysisDependency selectiveDependency = dependerDependencyElement.getDependency();
			
			Optional<String> nonTransitiveJarClassifier = findNonTransitiveJarClassifier(selectiveDependency);

			if (nonTransitiveJarClassifier.isPresent()) 
				return false;
			
			return true;
		}
		
		public String extractCustomScope(AnalysisDependency dependency) {
			return findNonTransitiveJarClassifier(dependency).orElse(null);
		}
		
		public Optional<String> findJarClassifier(AnalysisDependency dependency) {
			String type = dependency.getType();
			if (type == null)
				type = "jar";
			
			String classifier = dependency.getClassifier();
			
			if (classifier == null)
				return Optional.empty();
			
			if (type.equals("jar"))
				return Optional.of(classifier);
			
			return Optional.empty();
		}
		
		public Optional<String> findNonTransitiveJarClassifier(AnalysisDependency dependency) {
			
			Optional<String> classifier = findJarClassifier(dependency);
			
			if (!classifier.isPresent())
				return Optional.empty();
			
			switch (classifier.get()) {
			case "javadoc":
			case "sources":
				return classifier;
			default:
				return Optional.empty();
			}
		}
	}
}
