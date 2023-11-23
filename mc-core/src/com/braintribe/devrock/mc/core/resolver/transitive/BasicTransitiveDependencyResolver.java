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
package com.braintribe.devrock.mc.core.resolver.transitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.Functions;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.download.PartEnricher;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactPartResolver;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.api.transitive.ArtifactPathElement;
import com.braintribe.devrock.mc.api.transitive.BoundaryHit;
import com.braintribe.devrock.mc.api.transitive.BuildRange;
import com.braintribe.devrock.mc.api.transitive.DependencyPathElement;
import com.braintribe.devrock.mc.api.transitive.ResolutionPathElement;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.resolver.common.AnalysisArtifactResolutionPreparation;
import com.braintribe.devrock.model.mc.reason.UnresolvedArtifact;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependency;
import com.braintribe.exception.Exceptions;
import com.braintribe.execution.SimpleThreadPoolBuilder;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledSolution;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.compiled.ImportSolution;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.declared.Relocation;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.Promise;
import com.braintribe.processing.async.impl.HubPromise;
import com.braintribe.utils.lcd.LazyInitialization;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * @author Dirk Scheffler
 * @author Pit Steinlin
 *
 * @implNote Think about modelling {@link AnalysisArtifact} having traversing index being maintained by this resolver
 */
public class BasicTransitiveDependencyResolver implements TransitiveDependencyResolver, LifecycleAware {
	private DependencyResolver dependencyResolver;
	private CompiledArtifactResolver directArtifactResolver;
	private CompiledArtifactResolver redirectAwareArtifactResolver;
	private ArtifactPartResolver artifactPartResolver;
	private ExecutorService executorService;
	private int threadPoolSize = 20;
	private final List<Reason> invalidationReasons = new ArrayList<>();
	private PartEnricher partEnricher;
	
	@Configurable
	public void setThreadPoolSize(int poolSize) {
		this.threadPoolSize = poolSize;
	}

	@Required @Configurable
	public void setPartEnricher(PartEnricher partEnricher) {
		this.partEnricher = partEnricher;
	}
	
	@Required @Configurable
	public void setArtifactDataResolver(ArtifactPartResolver artifactPartResolver) {
		this.artifactPartResolver = artifactPartResolver;
	}
	
	@Required @Configurable
	public void setDirectArtifactResolver(CompiledArtifactResolver artifactResolver) {
		this.directArtifactResolver = artifactResolver;
	}
	
	@Required @Configurable
	public void setRedirectAwareArtifactResolver(CompiledArtifactResolver redirectAwareArtifactResolver) {
		this.redirectAwareArtifactResolver = redirectAwareArtifactResolver;
	}
	
	@Required @Configurable
	public void setDependencyResolver(DependencyResolver dependencyResolver) {
		this.dependencyResolver = dependencyResolver;
	}
	
	@Override
	public void postConstruct() {
		
		BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>();
		executorService = SimpleThreadPoolBuilder.newPool().poolSize(threadPoolSize, threadPoolSize).workQueue(workQueue).build();		
	}
	
	@Override
	public void preDestroy() {
		executorService.shutdownNow();
	}

	@Override
	public AnalysisArtifactResolution resolve(TransitiveResolutionContext context, Iterable<? extends CompiledTerminal> terminals) {
		return new StatefulTransitiveResolver(context, terminals).resolve();
	}
	
	/**
	 * PoiIdm stands for Parent Or Import Identity Management and is used to process parent/import linkage for each relevant artifact only once
	 * @author dirk.scheffler
	 *
	 */
	private static class PoiIdm {
		AnalysisArtifact artifact;
		boolean processed;
		public PoiIdm(AnalysisArtifact artifact) {
			super();
			this.artifact = artifact;
		}
	}
	

	
	/*
	 * 
	 */
	public interface InternalTransitiveResolutionContext {
		InternalTransitiveResolutionContext getParent();
		Predicate<ArtifactIdentification> getExclusionFilter();
		ConditionalTraversingScope getExclusionScope();
		String getDescriptor();
	}
	
	/*
	 * 
	 */
	private class StatefulTransitiveResolver {
		
		private final Iterable<? extends CompiledTerminal> terminals;
		private final Map<EqProxy<CompiledDependencyIdentification>, Promise<Maybe<CompiledArtifact>>> compiledArtifactPromises = new ConcurrentHashMap<>();
		private final Map<InternalTransitiveResolutionContext, Throwable> errors = new ConcurrentHashMap<>();
		private final Map<EqProxy<CompiledArtifactIdentification>, AnalysisArtifactProcessing> analysisArtifactProcessings = new ConcurrentHashMap<>();
		private volatile int asyncProcesses;
		private final Object asyncMonitor = new Object();
		private final Predicate<? super AnalysisArtifact> artifactFilter;
		private final Predicate<? super AnalysisDependency> dependencyFilter;
		private final boolean includeParentDependencies;
		private final boolean includeImportDependencies;
		private final boolean includeStandardDependencies;
		private final boolean includeRelocationDependencies;
		private final boolean respectExclusions;
		private final TransitiveResolutionContext resolutionContext;
		private ConditionalTraversingScope globalTraversingScope;
		
		public StatefulTransitiveResolver(TransitiveResolutionContext resolutionContext, Iterable<? extends CompiledTerminal> terminals) {
			super();
			this.resolutionContext = resolutionContext;
			this.terminals = terminals;
			artifactFilter = nullSafePredicate(resolutionContext.artifactFilter());
			dependencyFilter = nullSafePredicate(resolutionContext.dependencyFilter());
			includeImportDependencies = resolutionContext.includeImportDependencies();
			includeParentDependencies = resolutionContext.includeParentDependencies();
			includeStandardDependencies = resolutionContext.includeStandardDependencies();
			includeRelocationDependencies = resolutionContext.includeRelocationDependencies();
			respectExclusions = resolutionContext.respectExclusions();
			
			Set<ArtifactIdentification> globalExclusions = resolutionContext.globalExclusions();
			
			if (globalExclusions != null && !globalExclusions.isEmpty()) {
				globalTraversingScope = new ConditionalTraversingScope(ConditionalTraversingScope.EMPTY);
				globalTraversingScope.addExclusions(globalExclusions);
			}
			else {
				globalTraversingScope = ConditionalTraversingScope.EMPTY;
			}
		}
		
		private ConditionalTraversingScope getGlobalTraversingScope() {
			return globalTraversingScope;
		}
		
		private <T> Predicate<T> nullSafePredicate(Predicate<T> filter) {
			return filter != null? filter: Functions.invariantTrue();
		}
		
		private <T,X> BiPredicate<T,X> nullSafeBiPredicate(BiPredicate<T,X> filter) {
			return filter != null? filter: Functions.positiveBiPredicate();
		}

		/**
		 * do yer magic 
		 * @return - the resulting {@link AnalysisArtifactResolution}
		 */
		public AnalysisArtifactResolution resolve() {
			AnalysisArtifactResolution resolution = AnalysisArtifactResolution.T.create();
			
			for (CompiledTerminal terminal: terminals) {
				final AnalysisTerminal analysisTerminal;
				
				if (terminal instanceof CompiledDependencyIdentification) {
					CompiledDependencyIdentification cdi = (CompiledDependencyIdentification)terminal;

					CompiledDependency dependency = cdi instanceof CompiledDependency? 
							(CompiledDependency)cdi:
							CompiledDependency.from(cdi);
					
					TransitiveResolutionDependencyContext context = new TransitiveResolutionDependencyContext(dependency);
					
					resolveDependency(context);

					analysisTerminal = context.getDependency();
				}
				else if (terminal instanceof CompiledArtifact) {
					analysisTerminal = resolveArtifact(null, (CompiledArtifact)terminal);
				}
				else 
					throw new IllegalStateException("Unkown CompiledTerminal kind: " + terminal);
				
				resolution.getTerminals().add(analysisTerminal);
			}				
			
			// wait for async processing
			waitForAsyncCompletion();
			
			// TODO: check error handling again to meet newest strategy of leniency behaviour
			
			if (!errors.isEmpty() && !resolutionContext.lenient()) {
				String errorMessage = buildErrorMessage();
				
				IllegalStateException ex = new IllegalStateException(errorMessage);
				errors.values().stream().distinct().forEach(ex::addSuppressed);
				throw ex;
			}
			
			if (!invalidationReasons.isEmpty()) {
				Reason collatorReason = AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolution);
				collatorReason.getReasons().addAll(invalidationReasons);
			}
			
			return new StatefulResultPostProcessing(resolution).process();
		}

		/**
		 * @return
		 */
		private String buildErrorMessage() {
			StringBuilder builder = new StringBuilder();
			
			for (Map.Entry<InternalTransitiveResolutionContext, Throwable> errorEntry: errors.entrySet()) {
				if (builder.length() > 0)
					builder.append("\n");
				
				Throwable throwable = errorEntry.getValue();
				
				builder.append("Error occurred at resolution path:\n\n");
				InternalTransitiveResolutionContext context = errorEntry.getKey();
				appendPath(context, builder);
				
				builder.append("\n");
				builder.append("  Message: ");
				String message = throwable.getMessage();
				if (message == null) {
					message = "<no message>";
				}
				builder.append(message.replace("\n", "\n  "));
				builder.append("\n");
			}
			
			return builder.toString();
		}

		private void appendPath(InternalTransitiveResolutionContext context, StringBuilder builder) {
			InternalTransitiveResolutionContext parent = context.getParent();
			
			if (parent != null) {
				appendPath(parent, builder);
			}
			

			builder.append("  " + context.getDescriptor());
			builder.append("\n");
			
		}

		private AnalysisArtifact resolveArtifact(TransitiveResolutionDependencyContext context, CompiledArtifact artifact) {
			// TODO : review here - because failed artifact is not identity-managed
			if (artifact.getInvalid()) {		

				Reason whyInvalid = artifact.getWhyInvalid();
				invalidationReasons.add(whyInvalid);

				AnalysisArtifact analysisArtifact = AnalysisArtifact.of(artifact);
				analysisArtifact.setFailure(whyInvalid);
				
				if (context != null) {
					context.analysisDependency.setFailure(whyInvalid);
					context.analysisDependency.setSolution(analysisArtifact);
				}
								
				return analysisArtifact;
			}
			
			AnalysisArtifactProcessing analysisArtifactProcessing = acquireAnalysisArtifactProcessing(artifact);
			AnalysisArtifact analysisArtifact = analysisArtifactProcessing.getAnalysisArtifact();
			
			if (!analysisArtifactProcessing.isFiltered())
				return null;
			
			PreliminaryArtifactContext preliminaryArtifactContext = new PreliminaryArtifactContext(context, analysisArtifact);
			
			if (!resolutionContext.artifactPathFilter().test(preliminaryArtifactContext))
				return null;

			TransitiveResolutionArtifactContext artifactContext = new TransitiveResolutionArtifactContext(context, analysisArtifact);
			
//			TODO: check if this happens based on context option
//			if (artifactContext.isCyclic()) {
//				Reason dependencyCycleReason = context.couldNotResolveReason(Reasons.build(DependencyCycle.T).text("Dependency cycle error for dependency path: " + artifactContext.asPathString()).toReason());
//				context.getDependency().setFailure(dependencyCycleReason);
//				
//				AnalysisArtifactResolutionPreparation.acquireCollatorReason(analysisArtifact).getReasons().add(dependencyCycleReason);
//				
//				return analysisArtifact;
//			}

			analysisArtifactProcessing.resolveArtifactOnce(artifactContext);
			
			return analysisArtifact;
		}


		/*
		 * post process the result of the parallel transitive resolvings and collate them  
		 */
		private class StatefulResultPostProcessing {
			
			private int dependencyOrder = 0;
			private int visitOrder = 0;
			private final AnalysisArtifactResolution resolution;
			private final Set<AnalysisArtifact> visited = new HashSet<>();
			private final Map<EqProxy<CompiledArtifactIdentification>, PoiIdm> analysisArtifacts = new HashMap<>();

			
			public StatefulResultPostProcessing(AnalysisArtifactResolution resolution) {
				this.resolution = resolution;
			}
			
			public AnalysisArtifactResolution process() {
				collectSolutionsAndOmittUnprocessedDependencies();
				
				joinParentsAndImports();
				
				applyBuildRange();
				
				PartEnrichingContext enrichingContext = resolutionContext.enrich();
				
				if (enrichingContext != null)
					partEnricher.enrich(enrichingContext, resolution.getSolutions());
				
				return resolution;
			}
			
			private void joinParentsAndImports() {
				for (AnalysisArtifact artifact: visited) {
					indexAnalysisArtifact(artifact);
				}
				
				for (AnalysisArtifact artifact: visited) {
					joinParentAndImports(artifact, true);
				}
			}
			
			
			private void indexAnalysisArtifact(AnalysisArtifact artifact) {
				CompiledArtifact origin = artifact.getOrigin();
				analysisArtifacts.put(HashComparators.compiledArtifactIdentification.eqProxy(origin), new PoiIdm(artifact));
			}

			private void joinParentAndImports(AnalysisArtifact analysisArtifact, boolean isParentTerminal) {
				CompiledArtifact compiledArtifact = analysisArtifact.getOrigin();
				
				// join parent
				CompiledSolution parentSolution = compiledArtifact.getParentSolution();

				if (parentSolution != null) {
					CompiledDependencyIdentification parentDependency = parentSolution.getDependency();
					joinParent(analysisArtifact, parentDependency, parentSolution);
				}
				
				if (isParentTerminal) {
					// join imports
					List<ImportSolution> importSolutions = compiledArtifact.getImportSolutions();
					
					for (ImportSolution importSolution: importSolutions) {
						joinImport(analysisArtifact, importSolution);
					}
				}
			}

			private void joinImport(AnalysisArtifact analysisArtifact, ImportSolution importSolution) {
				// create and link dependency to importer
				CompiledDependencyIdentification importDependency = importSolution.getDependency();

				AnalysisDependency importAnalysisDependency = AnalysisDependency.from(CompiledDependency.from(importDependency, "import"));
				importAnalysisDependency.setDepender(analysisArtifact);
				importAnalysisDependency.setDeclarator(acquireParentOrImportAnalysisArtifact(importSolution.getDeclaringParent()));
				analysisArtifact.getImports().add(importAnalysisDependency);
				
				if (importSolution.hasFailed())
					importAnalysisDependency.setFailure(importSolution.getFailure());
				
				// link dependency to solution
				CompiledArtifact compiledSolution = importSolution.getSolution();
				if (compiledSolution == null)
					return;
				
				AnalysisArtifact importAnalysisArtifact = acquireParentOrImportAnalysisArtifact(compiledSolution);
						
				importAnalysisDependency.setSolution(importAnalysisArtifact);
				importAnalysisArtifact.getImporters().add(importAnalysisDependency);
			}

			private void joinParent(AnalysisArtifact analysisArtifact, CompiledDependencyIdentification parentDependency,
					CompiledSolution parentSolution) {
				// create and link dependency to depender
				AnalysisDependency parentAnalysisDependency = AnalysisDependency.from(CompiledDependency.from(parentDependency, "parent"));
				parentAnalysisDependency.setDepender(analysisArtifact);
				analysisArtifact.setParent(parentAnalysisDependency);
				if (parentSolution.hasFailed())
					parentAnalysisDependency.setFailure(parentSolution.getFailure());

				// link dependency to solution
				CompiledArtifact compiledSolution = parentSolution.getSolution();
				if (compiledSolution == null)
					return;
				
				AnalysisArtifact parentAnalysisArtifact = acquireParentOrImportAnalysisArtifact(compiledSolution);
				
				parentAnalysisDependency.setSolution(parentAnalysisArtifact);
				parentAnalysisArtifact.getParentDependers().add(parentAnalysisDependency);
			}
			
			private AnalysisArtifact acquireParentOrImportAnalysisArtifact(CompiledArtifact compiledArtifact) {
				PoiIdm poiIdm = analysisArtifacts.computeIfAbsent(HashComparators.compiledArtifactIdentification.eqProxy(compiledArtifact), k -> new PoiIdm(AnalysisArtifact.of(compiledArtifact)));

				if (poiIdm.processed)
					return poiIdm.artifact;
				
				poiIdm.processed = true;
				
				joinParentAndImports(poiIdm.artifact, false);
				
				return poiIdm.artifact;
			}

			// START OF PBDR build range logic
			private void applyBuildRange() {
				BuildRange buildRange = resolutionContext.buildRange();
				
				if (buildRange == null)
					return;
				
				Set<AnalysisArtifact> harvest = new HashSet<>();
				Set<AnalysisArtifact> visited = new HashSet<>();
				
				for (AnalysisTerminal terminal: resolution.getTerminals()) {
					final AnalysisArtifact terminalArtifact;
					
					if (terminal instanceof AnalysisDependency) {
						terminalArtifact = ((AnalysisDependency) terminal).getSolution();
					}
					else {
						terminalArtifact = (AnalysisArtifact)terminal;
					}
					
					if (terminalArtifact != null)
						harvest(terminalArtifact, harvest, visited);
				}
				
				resolution.setSolutions(harvest.stream().sorted(Comparator.comparing(AnalysisArtifact::getDependencyOrder)).collect(Collectors.toList()));
			}
			
			private boolean harvest(AnalysisArtifact solution, Set<AnalysisArtifact> harvest, Set<AnalysisArtifact> visited) {
				if (!visited.add(solution))
					return harvest.contains(solution);
				
				BuildRange buildRange = resolutionContext.buildRange();
				
				BoundaryHit upperHit = buildRange.upperBound().apply(solution.getOrigin());
				BoundaryHit lowerHit = buildRange.lowerBound().apply(solution.getOrigin());
				
				switch (upperHit) {
					case closed:
						harvest.add(solution);
						//$FALL-THROUGH$
					case open:
						break;
						
					default:
						break;
				}
				
				switch (lowerHit) {
					case closed:
						harvest.add(solution);
						//$FALL-THROUGH$
					case open:
						return true;
						
					default:
						break;
				}
				
				List<AnalysisDependency> dependencies = solution.getDependencies();
				
				if (dependencies.isEmpty()) {
					if (buildRange.lowerBound().apply(BuildRange.boundaryFloor) == BoundaryHit.open) {
						harvest.add(solution);
						return true;
					}
					return false;
				}

				boolean connectedToLowerBound = false;
				
				for (AnalysisDependency dependency: dependencies) {
					AnalysisArtifact depSolution = dependency.getSolution();
					
					if (depSolution != null && harvest(depSolution, harvest, visited) && upperHit == BoundaryHit.none) {
						harvest.add(solution);
						connectedToLowerBound = true;
					}
				}
				
				return connectedToLowerBound;
				
			}
			// END OF PBDR build range logic

			
			private void collectSolutionsAndOmittUnprocessedDependencies() {
				List<AnalysisArtifact> solutions = resolution.getSolutions();
				for (Iterator<AnalysisTerminal> it = resolution.getTerminals().iterator(); it.hasNext();) {
					AnalysisTerminal terminal = it.next();

					if (terminal instanceof AnalysisDependency) {
						AnalysisDependency dependency = (AnalysisDependency)terminal;
						collectSolutionsAndOmittUnprocessedDependencies(null, dependency, solutions, () -> it.remove());
					}
					else if (terminal instanceof AnalysisArtifact) {
						AnalysisArtifact artifact = (AnalysisArtifact)terminal;
						collectSolutionsAndOmittUnprocessedDependencies(artifact, solutions, true);
					}
				}
			}
			
			private void collectSolutionsAndOmittUnprocessedDependencies(AnalysisArtifact solution, List<AnalysisArtifact> solutions, boolean terminalArtifact) {		
				
				if (!visited.add( solution))
					return;
				
				if (solution.hasFailed()) {
					Reason collatorReason = AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolution);
					collatorReason.getReasons().add(solution.getFailure());
					resolution.getIncompleteArtifacts().add(solution);
				}
				
				solution.setVisitOrder(visitOrder++);
				
				Iterator<AnalysisDependency> it = solution.getDependencies().iterator();
				
				while (it.hasNext()) {
					AnalysisDependency dependency = it.next();
					collectSolutionsAndOmittUnprocessedDependencies(solution, dependency, solutions, () -> it.remove());
				}
				
				solution.setDependencyOrder(dependencyOrder++);
				
				if (!terminalArtifact)
					solutions.add(solution);
			}
			
			private void collectSolutionsAndOmittUnprocessedDependencies(AnalysisArtifact depender, AnalysisDependency dependency, List<AnalysisArtifact> solutions, Runnable remover) {
				AnalysisArtifact dependencySolution = dependency.getSolution();
				if (dependencySolution != null) {
					// TODO : review this -> it means that resolved artifact can be invalid, and therefore need to added to the incompletes
					if (!dependency.hasFailed()) {
						collectSolutionsAndOmittUnprocessedDependencies(dependencySolution, solutions, false);
					}
					else {
						// artifact which was correctly resolved was in turn invalid for any reason.
						resolution.getIncompleteArtifacts().add( dependencySolution); // add the artifact with problems
						if (depender != null) {
							// if there is a depender, add the depender artifact as incomplete as well
							resolution.getIncompleteArtifacts().add(depender); 
						}
					}
				}
				else {
					if (dependency.getFailure() == null) {
						remover.run();
						resolution.getFilteredDependencies().add(dependency);
					}
					else {
						resolution.getUnresolvedDependencies().add(dependency);
						
						//build a reason why this artifact is incomplete
						if (depender != null) {
							Reason artifactCollatorReason = AnalysisArtifactResolutionPreparation.acquireCollatorReason( depender);
							artifactCollatorReason.getReasons().add( dependency.getFailure());
							if (resolution.getIncompleteArtifacts().add(depender)) {
								AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolution).getReasons().add(artifactCollatorReason);
							}
						}
						else {
							//build a reason why this resolution is incomplete
							Reason resolutionCollatorReason = AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolution);
							resolutionCollatorReason.getReasons().add( dependency.getFailure());
						}
					}
				}
			}
		}

		private Promise<Maybe<CompiledArtifact>> resolveDependencyIdentificationAsync(CompiledDependencyIdentification dependency) {
			EqProxy<CompiledDependencyIdentification> mapKey = HashComparators.compiledDependencyIdentification.eqProxy(dependency);
			return compiledArtifactPromises.computeIfAbsent(mapKey, d -> submit(() -> resolveDependencyIdentificationSync(dependency)));
		}
		
		private Maybe<CompiledArtifact> resolveDependencyIdentificationSync(CompiledDependencyIdentification dependency) {
			Maybe<CompiledArtifactIdentification> optionalIdentification = dependencyResolver.resolveDependency(dependency);
			
			if (optionalIdentification.isUnsatisfied()) {
				return optionalIdentification.whyUnsatisfied().asMaybe();
			}
			
			CompiledArtifactIdentification artifactIdentification = optionalIdentification.get();
			
			CompiledArtifactResolver effectiveArtifactResolver = includeRelocationDependencies? directArtifactResolver: redirectAwareArtifactResolver;
			
			Maybe<CompiledArtifact> optionalArtifact = effectiveArtifactResolver.resolve(artifactIdentification);
			
			if (optionalArtifact.isUnsatisfied()) {
				return TemplateReasons.build(UnresolvedArtifact.T).enrich(r -> r.setArtifact(artifactIdentification)) //
						.cause(optionalArtifact.whyUnsatisfied()).toMaybe();
			}
			
			CompiledArtifact compiledArtifact = optionalArtifact.get();
			
			return Maybe.complete(compiledArtifact);
		}
		
		private void resolveDependency(TransitiveResolutionDependencyContext context) {
			CompiledDependency compiledDependency = context.getCompiledDependency();
			AnalysisDependency dependency = context.getDependency();
			
			if (compiledDependency.getInvalid()) {
				dependency.setFailure(compiledDependency.getWhyInvalid());
				return;
			}
			
			resolveDependencyIdentificationAsync(context.getCompiledDependency()).get(AsyncCallback.of(
				artifactPotential -> {
					if (artifactPotential.isUnsatisfied()) {
						context.getDependency().setFailure(context.couldNotResolveReason(artifactPotential.whyUnsatisfied()));
						return;
					}
					
					CompiledArtifact artifact = artifactPotential.get();
					
					AnalysisArtifact resolveArtifact = resolveArtifact(context, artifact);
				},
				t -> {
					// unresolved dependency
					dependency.setFailure(context.couldNotResolveReason(InternalError.from(t)));
					errors.put(context, t);
				}
			));
		}

		private AnalysisArtifactProcessing acquireAnalysisArtifactProcessing(CompiledArtifact artifact) {
			EqProxy<CompiledArtifactIdentification> key = HashComparators.compiledArtifactIdentification.eqProxy(artifact);
			AnalysisArtifactProcessing analysisArtifactProcessing = analysisArtifactProcessings.computeIfAbsent(key, k -> new AnalysisArtifactProcessing(artifact));
			return analysisArtifactProcessing;
		}
		
		private void onAsyncStart() {
			synchronized (asyncMonitor) {
				asyncProcesses++;
			}
		}
		
		private void onAsyncStop() {
			synchronized (asyncMonitor) {
				if (--asyncProcesses == 0)
					asyncMonitor.notify();
			}
		}
		
		private void waitForAsyncCompletion() {
			synchronized (asyncMonitor) {
				if (asyncProcesses == 0)
					return;
				
				try {
					asyncMonitor.wait();
				} catch (InterruptedException e) {
					Exceptions.unchecked(e, "Unexpected interruption");
				}
			}
		}
		
		private <T> Promise<T> submit(Supplier<T> supplier) {
			onAsyncStart();
			HubPromise<T> promise = new HubPromise<>();
			executorService.submit(() -> {
				try {
					promise.accept(supplier.get());
				}
				catch (Throwable e) {
					promise.onFailure(e);
				}
				finally {
					onAsyncStop();
				}
			});
			
			return promise;
		}
		/*
		 * 
		 */
		private class AnalysisArtifactProcessing {
			//		
			private final AnalysisArtifact analysisArtifact;
			private final Set<Object> processedScopes = new HashSet<>();
			private final CompiledArtifact artifact;
			private final LazyInitialized<Boolean> filtered = new LazyInitialized<>(this::checkFiltered);
			private final LazyInitialization pomInitializer = new LazyInitialization(this::initPomPart);
			
			
			public AnalysisArtifactProcessing(CompiledArtifact artifact) {
				
				this.artifact = artifact;
				if (artifact.getInvalid()) {
					// TODO: if lenient, the pom compiler will only have flagged this as invalid... 
					
				}
				analysisArtifact = AnalysisArtifact.of(artifact);
				
				List<AnalysisDependency> analysisDependencies = analysisArtifact.getDependencies();
				
				getDependencies().map(this::buildAnalysisDependency).peek(analysisDependencies::add).forEach(d -> d.setDepender(analysisArtifact));
			}
			
			private AnalysisDependency buildAnalysisDependency(CompiledDependency dependency) {
				// TODO : if lenient, the dependency may be flagged as invalid
				AnalysisDependency analysisDependency = AnalysisDependency.T.create();
				analysisDependency.setDepender(analysisArtifact);
				analysisDependency.setOrigin(dependency);
				analysisDependency.setGroupId(dependency.getGroupId());
				analysisDependency.setArtifactId(dependency.getArtifactId());
				analysisDependency.setClassifier(dependency.getClassifier());
				analysisDependency.setType(dependency.getType());
				analysisDependency.setVersion(dependency.getVersion().asString());
				analysisDependency.setScope(dependency.getScope());
				analysisDependency.setOptional(dependency.getOptional());
				
				return analysisDependency;
			}
			
			private Stream<CompiledDependency> getDependencies() {
				Stream<CompiledDependency> dependencyStream = Stream.empty();
				
				/* Despite that the compiled artifact we are processing has already resolved parents and imports if required with relocation awareness.
				 * Thus we reprocess its parents and imports here on a higher level to see and track relocations as dependency for parents and imports.
				 */
				
				if (includeParentDependencies && artifact.getParent() != null) {
					dependencyStream = Stream.concat(dependencyStream, Stream.of(CompiledDependency.from(artifact.getParent(), "parent")));
				}
				
				if (includeImportDependencies) {
					dependencyStream = Stream.concat(dependencyStream, artifact.getImports().stream().map(d -> CompiledDependency.from(d, "import")));
				}
				
				if (includeStandardDependencies) {
					dependencyStream = Stream.concat(dependencyStream, artifact.getDependencies().stream());
				}
				
				if (includeRelocationDependencies) {
					Relocation relocation = artifact.getRelocation();
					if (relocation != null) {
						//TODO: remove if correctly once done by the PomCompiler
						Relocation sanitizedRelocation = Relocation.from(relocation, VersionedArtifactIdentification.create( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion().asString()));
						dependencyStream = Stream.concat(dependencyStream, Stream.of(CompiledDependency.from(sanitizedRelocation, "relocation")));
					}
				}

				return dependencyStream;
			}
			
			public boolean isFiltered() {
				return filtered.get();
			}
			
			private boolean checkFiltered() {
				return artifactFilter.test(analysisArtifact);
			}
			
			public AnalysisArtifact getAnalysisArtifact() {
				return analysisArtifact;
			}
			
			public void resolveArtifactOnce(TransitiveResolutionArtifactContext context) {
				synchronized (processedScopes) {
					if (!processedScopes.add(context.getTraversingScope()))
						return;
				}
				
				resolveArtifact(context);
			}
			
			 
			
			private void resolveArtifact(TransitiveResolutionArtifactContext context) {
				
				ensurePomPart(context);
				
				if (!resolutionContext.artifactTransitivityPredicate().test(context))
					return;
				
				for (AnalysisDependency analysisDependency: analysisArtifact.getDependencies()) {
					CompiledDependency dependency = analysisDependency.getOrigin();
					ConditionalTraversingScope scope = context.getExclusionScope();

					/* TODO: investigate how to properly do redirects here with scope.applyRedirect(dependency) which
					 * would mean to add further analysis dependencies identity managed and synchronized
					 * 
					 * OR kill the idea of partial-tree-global redirects completely !!!
					 */
					
					TransitiveResolutionDependencyContext dependencyContext = new TransitiveResolutionDependencyContext(context, analysisDependency);
					
					if (scope.isExcluded(dependency))
						continue;

					if (!resolutionContext.dependencyPathFilter().test(dependencyContext))
						continue;
					
					if (!acquireDependencyProcessing(analysisDependency).isFiltered())
						continue;
					
					resolveDependency(dependencyContext);
				}
			}

			private void ensurePomPart(TransitiveResolutionArtifactContext context) {
				if (context.getParent() == null)
					return;

				pomInitializer.run();
			}
			
			private void initPomPart() {
				// TODO: handle Reasons
				Maybe<ArtifactDataResolution> pomPart = artifactPartResolver.resolvePart(artifact, PartIdentifications.pom, null);
				ArtifactDataResolution artifactDataResolution = pomPart.get();
				
				Part part = Part.T.create();
				part.setResource(artifactDataResolution.getResource());
				part.setType( "pom");
				part.setRepositoryOrigin(artifactDataResolution.repositoryId());
				
				analysisArtifact.getParts().put(PartIdentifications.pom.asString(), part);
			}

		}
		
		private DependencyProcessing acquireDependencyProcessing(AnalysisDependency analysisDependency) {
			return dependencyProcessings.computeIfAbsent(analysisDependency, DependencyProcessing::new);
		}
		
		private final Map<AnalysisDependency, DependencyProcessing> dependencyProcessings = new ConcurrentHashMap<>();
		
		private class DependencyProcessing {
			private final LazyInitialized<Boolean> filtered = new LazyInitialized<>(this::checkFiltered);
			
			private final AnalysisDependency analysisDependency;
			
			public DependencyProcessing(AnalysisDependency analysisDependency) {
				super();
				this.analysisDependency = analysisDependency;
			}

			public boolean isFiltered() {
				return filtered.get();
			}
			
			private boolean checkFiltered() {
				return dependencyFilter.test(analysisDependency);
			}
		}

		/*
		 * 
		 */
		
		private abstract class AbstractTransitiveResolutionContext implements ResolutionPathElement {
			
			protected abstract String getPathConnector();
			
			@Override
			public abstract AbstractTransitiveResolutionContext getParent();
			
			@Override
			public String asPathString() {
				StringBuilder builder = new StringBuilder();
				buildPathString(builder);
				return builder.toString();
			}
			
			private void buildPathString(StringBuilder builder) {
				AbstractTransitiveResolutionContext parent = getParent();
				if (parent != null) {
					parent.buildPathString(builder);
					builder.append(parent.getPathConnector());
				}
				
				builder.append(asString());
			}
		}
		
		/** 
		 * context for the dependency node within the tree 
		 * @author pit / dirk
		 *
		 */
		private class TransitiveResolutionDependencyContext extends AbstractTransitiveResolutionContext implements InternalTransitiveResolutionContext, DependencyPathElement {
			private TransitiveResolutionArtifactContext parent;
			private final CompiledDependency dependency;
			private AnalysisDependency analysisDependency;
			private Predicate<ArtifactIdentification> exclusionFilter;
			private ConditionalTraversingScope exclusionScope;
			private final Object customScope;
			
			public TransitiveResolutionDependencyContext(CompiledDependency dependency) {
				this.dependency = dependency;
				primeAnalysisDependency();
				
				exclusionScope = new ConditionalTraversingScope(getGlobalTraversingScope());
				
				if (respectExclusions && dependency.getExclusions() != null)
					exclusionScope.addExclusions(dependency.getExclusions());
				
				customScope = resolutionContext.customScopeSupplier().apply(analysisDependency);
			}

			public TransitiveResolutionDependencyContext(TransitiveResolutionArtifactContext parent, AnalysisDependency analysisDependency) {
				this.parent = parent;
				this.analysisDependency = analysisDependency;
				this.dependency = analysisDependency.getOrigin();

				Predicate<ArtifactIdentification> parentExclusionFilter = parent.getExclusionFilter();
				if (respectExclusions && !dependency.getExclusions().isEmpty()) {
					exclusionScope = new ConditionalTraversingScope(parent.getExclusionScope());
					exclusionScope.addExclusions(dependency.getExclusions());
					if (parentExclusionFilter != null) {
						exclusionFilter = Exclusions.predicate(dependency).and(parentExclusionFilter);
					}
					else {
						exclusionFilter = Exclusions.predicate(dependency);
					}
				}
				else {
					exclusionScope = parent.getExclusionScope();
					exclusionFilter = parentExclusionFilter;
				}
				
				customScope = resolutionContext.customScopeSupplier().apply(analysisDependency);
			}

			private void primeAnalysisDependency() {
				analysisDependency = AnalysisDependency.T.create();
				analysisDependency.setGroupId(dependency.getGroupId());
				analysisDependency.setArtifactId(dependency.getArtifactId());
				analysisDependency.setClassifier(dependency.getClassifier());
				analysisDependency.setType(dependency.getType());
				analysisDependency.setOrigin(dependency);
				analysisDependency.setVersion(dependency.getVersion().asString());
				analysisDependency.setScope(dependency.getScope());
				analysisDependency.setOptional(dependency.getOptional());
			}
			
			public Reason couldNotResolveReason(Reason why) {
				return TemplateReasons.build(UnresolvedDependency.T).enrich(r -> r.setDependency(dependency)).cause(why).toReason();
			}
			
			@Override
			protected String getPathConnector() {
				return " => ";
			}
			
			public Object getCustomScope() {
				return customScope;
			}
			
			@Override
			public String getDescriptor() {
				return "depended: " + dependency.getGroupId() + ":" + dependency.getArtifactId() + "#" + dependency.getVersion().asString() + " scope: " + dependency.getScope() + (dependency.getClassifier() != null? " classifier: " + dependency.getClassifier(): "") + " type: " + dependency.getType();
			}
			
			@Override
			public ConditionalTraversingScope getExclusionScope() {
				return exclusionScope;
			}
			
			@Override
			public Predicate<ArtifactIdentification> getExclusionFilter() {
				return exclusionFilter;
			}
			
			public ArtifactIdentification getArtifactIdentification() {
				return dependency;
			}
			
			@Override
			public TransitiveResolutionArtifactContext getParent() {
				return parent;
			}
			
			public CompiledDependency getCompiledDependency() {
				return dependency;
			}
			
			@Override
			public AnalysisDependency getDependency() {
				return analysisDependency;
			}
		}
		
		private class PreliminaryArtifactContext extends AbstractTransitiveResolutionContext implements ArtifactPathElement {
			private final TransitiveResolutionDependencyContext parent;
			private final AnalysisArtifact artifact;

			public PreliminaryArtifactContext(TransitiveResolutionDependencyContext parent, AnalysisArtifact artifact) {
				super();
				this.parent = parent;
				this.artifact = artifact;
			}

			@Override
			public TransitiveResolutionDependencyContext getParent() {
				return parent;
			}

			@Override
			public AnalysisArtifact getArtifact() {
				return artifact;
			}

			@Override
			protected String getPathConnector() {
				return " -> ";
			}
		}
		
		/*
		 * 
		 */
		/**
		 * context for the artifact node of the tree 
		 * @author pit / dirk
		 *
		 */
		private class TransitiveResolutionArtifactContext extends AbstractTransitiveResolutionContext implements InternalTransitiveResolutionContext, ArtifactPathElement {
			private final TransitiveResolutionDependencyContext parent;
			private final CompiledArtifact artifact;
			private final AnalysisArtifact analysisArtifact;
			private ConditionalTraversingScope exclusionScope;
			private Predicate<ArtifactIdentification> exclusionFilter = Functions.invariantFalse();
			private final Object customScope;
			
			public TransitiveResolutionArtifactContext(TransitiveResolutionDependencyContext parent,
					AnalysisArtifact analysisArtifact) {
				this.parent = parent;
				this.analysisArtifact = analysisArtifact;
				this.artifact = analysisArtifact.getOrigin();

				if (parent != null) {
					AnalysisDependency analysisDependency = parent.getDependency();
					analysisDependency.setSolution(analysisArtifact);
					synchronized (analysisArtifact) {
						Set<AnalysisDependency> dependers = analysisArtifact.getDependers();
						dependers.add(analysisDependency);
					}
				}
				
				ConditionalTraversingScope parentScope = parent != null? parent.getExclusionScope(): getGlobalTraversingScope();
				Predicate<ArtifactIdentification> parentExclusionFilter = parent != null? parent.getExclusionFilter(): Functions.invariantFalse();
	
				
				LazyInitialized<ConditionalTraversingScope> scopeSupplier = new LazyInitialized<>(() -> new ConditionalTraversingScope(parentScope));

				if (respectExclusions && !artifact.getExclusions().isEmpty()) {
					scopeSupplier.get().addExclusions(artifact.getExclusions());
					// TODO : review this fix - appears if global exclusions are given, there's no parentExclusionFilter then
					if (parentExclusionFilter != null) {
						exclusionFilter = Exclusions.predicate(artifact).and(parentExclusionFilter);
					}
					else {
						exclusionFilter = Exclusions.predicate(artifact);
					}
				}
				else {
					exclusionFilter = parentExclusionFilter;
				}
				
				if (!artifact.getArtifactRedirects().isEmpty()) {
					scopeSupplier.get().addRedirects(artifact.getArtifactRedirects());
				}
				
				if (scopeSupplier.isInitialized())
					exclusionScope = scopeSupplier.get();
				else
					exclusionScope = parentScope;
				
				customScope = parent != null? parent.getCustomScope(): null;
			}
			
			@Override
			protected String getPathConnector() {
				return " -> ";
			}
			
			public CompiledDependency applyRedirect(CompiledDependency dependency) {
				return exclusionScope.applyRedirect(dependency);
			}
			
			@Override
			public String getDescriptor() {
				return "artifact: " + artifact.asString();
			}
			
			@Override
			public ConditionalTraversingScope getExclusionScope() {
				return exclusionScope;
			}
			
			public Object getTraversingScope() {
				return Pair.of(exclusionScope, customScope);
			}
			
			@Override
			public Predicate<ArtifactIdentification> getExclusionFilter() {
				return exclusionFilter;
			}
			
			@Override
			public TransitiveResolutionDependencyContext getParent() {
				return parent;
			}
			public ArtifactIdentification getArtifactIdentification() {
				return artifact;
			}
			
			public CompiledArtifact getCompiledArtifact() {
				return artifact;
			}
			
			@Override
			public AnalysisArtifact getArtifact() {
				return analysisArtifact;
			}

			public boolean isCyclic() {
				
				TransitiveResolutionArtifactContext currentParent = getRequestorContext();
				
				while (currentParent != null) {
					
					if (HashComparators.compiledArtifactIdentification.compare(artifact, currentParent.getCompiledArtifact())) {
						return true;
					}
					
					currentParent = currentParent.getRequestorContext();
				}
				
				return false;
			}
			
			public TransitiveResolutionArtifactContext getRequestorContext() {
				if (parent == null)
					return null;
				
				TransitiveResolutionArtifactContext artifactParent = parent.getParent();
				
				return artifactParent;
			}
		}

	}
	/*
	 * 
	 */
	/**
	 * 
	 * @author pit / dirk
	 *
	 */
	public static class ConditionalTraversingScope {
		public static final  ConditionalTraversingScope EMPTY = new ConditionalTraversingScope();
		
		private Set<EqProxy<ArtifactIdentification>> exclusions;
		private Map<EqProxy<CompiledDependencyIdentification>, CompiledDependencyIdentification> redirects;
		private Predicate<ArtifactIdentification> predicate;
		
		private ConditionalTraversingScope() {
			exclusions = Collections.emptySet();
			predicate = Functions.invariantFalse();
		}
		
		public Predicate<ArtifactIdentification> getPredicate() {
			return predicate;
		}
		
		public Map<EqProxy<CompiledDependencyIdentification>, CompiledDependencyIdentification> getRedirects() {
			return redirects;
		}
		
		public ConditionalTraversingScope(ConditionalTraversingScope parent) {
			if (parent != null) {
				this.redirects = parent.getRedirects();
				this.exclusions = parent.getExclusions();
				this.predicate = parent.getPredicate();
			}
			else {
				this.redirects = Collections.emptyMap();
				this.exclusions = Collections.emptySet();
				this.predicate = Functions.invariantFalse();
			}
		}
		
		public void addExclusions(Set<ArtifactIdentification> exclusions) {
			this.exclusions = new HashSet<>(this.exclusions);
			exclusions.stream().map(HashComparators.artifactIdentification::eqProxy).forEach(this.exclusions::add);
			predicate = Exclusions.predicate(this.exclusions.stream().map(EqProxy::get));
		}
		
		public void addRedirects(Map<CompiledDependencyIdentification, CompiledDependencyIdentification> redirects) {
			// TODO : review this - old code was not functional
			this.redirects = new HashMap<>(redirects.size());			
			for (Map.Entry<CompiledDependencyIdentification, CompiledDependencyIdentification> entry: redirects.entrySet()) {
				this.redirects.put(HashComparators.compiledDependencyIdentification.eqProxy(entry.getKey()), entry.getValue());
			}
		}
		
		public CompiledDependency applyRedirect(CompiledDependency dependency) {
			if (redirects.isEmpty())
				return null;
			
			CompiledDependencyIdentification redirect = redirects.get(HashComparators.compiledDependencyIdentification.eqProxy(dependency));
			
			if (redirect == null)
				return null;

			// TODO: what about the other criteria of a CompiledDependency such as scope, exclusions, type 
			CompiledDependency redirectedDependency = CompiledDependency.from(redirect);
			redirectedDependency.setOrigin(dependency.getOrigin());
			return redirectedDependency;
		}
		
		public Set<EqProxy<ArtifactIdentification>> getExclusions() {
			return exclusions;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((exclusions == null) ? 0 : exclusions.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConditionalTraversingScope other = (ConditionalTraversingScope) obj;
			if (exclusions == null) {
				if (other.exclusions != null)
					return false;
			} else if (!exclusions.equals(other.exclusions))
				return false;
			return true;
		}

		public boolean isExcluded(ArtifactIdentification artifactIdentification) {
			return predicate.test(artifactIdentification);
		}


	}
	
}
