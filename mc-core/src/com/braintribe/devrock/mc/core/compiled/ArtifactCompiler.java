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
package com.braintribe.devrock.mc.core.compiled;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactIdentificationAssocResolver;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.DeclaredArtifactCompiler;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.core.commons.PiCommons;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.model.mc.reason.ArtifactCompilationFailed;
import com.braintribe.devrock.model.mc.reason.ArtifactIdentificationMismatch;
import com.braintribe.devrock.model.mc.reason.DependencyManagementCompilationFailed;
import com.braintribe.devrock.model.mc.reason.InvalidArtifactIdentification;
import com.braintribe.devrock.model.mc.reason.InvalidImport;
import com.braintribe.devrock.model.mc.reason.InvalidParent;
import com.braintribe.devrock.model.mc.reason.InvalidParentReference;
import com.braintribe.devrock.model.mc.reason.MalformedArtifactDescriptor;
import com.braintribe.devrock.model.mc.reason.MalformedDependency;
import com.braintribe.devrock.model.mc.reason.MalformedManagedDependency;
import com.braintribe.devrock.model.mc.reason.ParentCompilationCycle;
import com.braintribe.devrock.model.mc.reason.UnresolvedImport;
import com.braintribe.devrock.model.mc.reason.UnresolvedManagedDependency;
import com.braintribe.devrock.model.mc.reason.UnresolvedParent;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.model.reason.essential.ParseError;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledSolution;
import com.braintribe.model.artifact.compiled.ImportSolution;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.declared.DeclaredDependency;
import com.braintribe.model.artifact.declared.DistributionManagement;
import com.braintribe.model.artifact.declared.ProcessingInstruction;
import com.braintribe.model.artifact.declared.Relocation;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.DependencyIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionRange;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.utils.lcd.NullSafe;

/**
 * compiles a pom - complete with parent/import, variable, dep-mgt resolving.
 * Will flag the {@link CompiledArtifact} as invalid if anything goes wrong 
 * @author pit / dirk
 *
 */
public class ArtifactCompiler implements CompiledArtifactResolver, DeclaredArtifactCompiler {
	private CompiledArtifactIdentificationAssocResolver<DeclaredArtifact> declaredArtifactResolver;
	private CompiledArtifactIdentificationAssocResolver<CompiledArtifact> compiledArtifactResolver;
	private DependencyResolver dependencyResolver;
	private static final Map<String, ExpressivePropertyMapping<?, ?>> expressivePropertyMappings = new HashMap<>();
	private static final YamlMarshaller marshaller = new YamlMarshaller();
	private ThreadLocal<InProgressManager> inProgressLocal = ThreadLocal.withInitial(InProgressManager::new);
	
	static  {
		ExpressivePropertyMapping< Map<String,String>, Map<CompiledDependencyIdentification, CompiledDependencyIdentification>> mappingForRedirects = new ExpressivePropertyMapping<>();
		mappingForRedirects.setDeclaredType( GMF.getTypeReflection().getMapType(EssentialTypes.TYPE_STRING, EssentialTypes.TYPE_STRING));
		mappingForRedirects.setProperty( CompiledArtifact.T.getProperty(CompiledArtifact.artifactRedirects));
		mappingForRedirects.setTransformerFunction( ExpressiveTransformations::transformArtifactRedirects);
		expressivePropertyMappings.put("artifact-redirects", mappingForRedirects);
		
		ExpressivePropertyMapping< List<String>, List<CompiledDependencyIdentification>> mappingDominants = new ExpressivePropertyMapping<>();
		mappingDominants.setDeclaredType( GMF.getTypeReflection().getListType(EssentialTypes.TYPE_STRING));
		mappingDominants.setProperty( CompiledArtifact.T.getProperty(CompiledArtifact.dominants));
		mappingDominants.setTransformerFunction( ExpressiveTransformations::transformDominants);
		expressivePropertyMappings.put("global-dominants", mappingDominants);
		
		ExpressivePropertyMapping< Set<String>, Set<ArtifactIdentification>> mappingExclusions = new ExpressivePropertyMapping<>();
		mappingExclusions.setDeclaredType( GMF.getTypeReflection().getSetType(EssentialTypes.TYPE_STRING));
		mappingExclusions.setProperty( CompiledArtifact.T.getProperty(CompiledArtifact.exclusions));
		mappingExclusions.setTransformerFunction( ExpressiveTransformations::transformExclusions);
		expressivePropertyMappings.put("global-exclusions", mappingExclusions);
	}
	
	@Required @Configurable
	public void setDeclaredArtifactResolver(
			CompiledArtifactIdentificationAssocResolver<DeclaredArtifact> declaredParentResolver) {
		this.declaredArtifactResolver = declaredParentResolver;
	}
	
	@Required @Configurable
	public void setCompiledArtifactResolver(
			CompiledArtifactIdentificationAssocResolver<CompiledArtifact> compiledArtifactResolver) {
		this.compiledArtifactResolver = compiledArtifactResolver;
	}
	
	@Required @Configurable
	public void setDependencyResolver(DependencyResolver dependencyResolver) {
		this.dependencyResolver = dependencyResolver;
	}
	
	@Override
	public Maybe<CompiledArtifact> resolve(CompiledArtifactIdentification compiledArtifactIdentification) {
		pushInProgress(compiledArtifactIdentification);
		try {
			return new StatefulArtifactCompiler(compiledArtifactIdentification).compile();
		}
		finally {
			popInProgress(compiledArtifactIdentification);
		}
	}
	
	private void pushInProgress(CompiledArtifactIdentification compiledArtifactIdentification) {
		inProgressLocal.get().push(compiledArtifactIdentification);
	}
	
	private void popInProgress(CompiledArtifactIdentification compiledArtifactIdentification) {
		InProgressManager inProgressManager = inProgressLocal.get();
		if (inProgressManager.pop(compiledArtifactIdentification)) {
			inProgressLocal.remove();
		}
	}
	
	private class InProgressManager {
		private Set<EqProxy<CompiledArtifactIdentification>> inProgress = new LinkedHashSet<>();
		
		public void push(CompiledArtifactIdentification cai) {
			inProgress.add(HashComparators.compiledArtifactIdentification.eqProxy(cai));
		}
		
		/**
		 * Pops the current CAI from stack and returns true if the stack got empty by this operation
		 * @return
		 */
		public boolean pop(CompiledArtifactIdentification cai) {
			inProgress.remove(HashComparators.compiledArtifactIdentification.eqProxy(cai));
			return inProgress.isEmpty();
		}

		public boolean isInProgress(CompiledArtifactIdentification cai) {
			return inProgress.contains(HashComparators.compiledArtifactIdentification.eqProxy(cai));
		}
		
		public List<CompiledArtifactIdentification> hasCycle(CompiledArtifactIdentification cai) {
			if (!isInProgress(cai))
				return null;
			
			// A -> (B -> C -> D -> B)
			
			List<CompiledArtifactIdentification> cycle = new ArrayList<>();
			boolean found = false;
			for (EqProxy<CompiledArtifactIdentification> proxy: inProgress) {
				CompiledArtifactIdentification curCai = proxy.get(); 
				
				if (found) {
					cycle.add(curCai);
				}
				else {
					if (HashComparators.compiledArtifactIdentification.compare(curCai, cai)) {
						cycle.add(curCai);
						found = true;
					}
				}
			}
			
			cycle.add(cai);
			
			return cycle;
		}
	}

	private Maybe<CompiledArtifact> resolveCompiledArtifactCycleAware(CompiledArtifactIdentification cai) {
		List<CompiledArtifactIdentification> cycle = inProgressLocal.get().hasCycle(cai);
		
		if (cycle != null) {
			return TemplateReasons.build(ParentCompilationCycle.T).assign(ParentCompilationCycle::setArtifacts, cycle).toMaybe();
		}
		
		return compiledArtifactResolver.resolve(cai);
	}
	
	private class StatefulArtifactCompiler {
		private CompiledArtifactIdentification compiledArtifactIdentification;
		private DeclaredArtifact declaredArtifact;
		private CompiledArtifact compiledArtifact = CompiledArtifact.T.create();
		private DeclaredArtifact effectiveDeclaredArtifact;
		private List<CompiledDependency> managedDependencies = compiledArtifact.getManagedDependencies();
		private Map<EqProxy<DependencyIdentification>, CompiledDependency> effectiveManagedDependencies = new HashMap<>();
		private LazyInitialized<Reason> artifactFailure = new LazyInitialized<>(this::initArtifactReason);
		private LazyInitialized<Reason> dependencyManagementCompilationFailure = new LazyInitialized<>(this::initDependencyManagementCompilationFailure);
		private DeclaredArtifact aggregatedDeclaredArtifact;
		
		/**
		 * This list holds a parent for each managed dependency in the order of their original declaration via the parent chain  
		 */
		private List<CompiledArtifact> managedDependencyDeclarators = new ArrayList<>();
		
		public StatefulArtifactCompiler(CompiledArtifactIdentification compiledArtifactIdentification) {
			super();
			this.compiledArtifactIdentification = compiledArtifactIdentification;
		}
		
		public StatefulArtifactCompiler(DeclaredArtifact declaredArtifact) {
			super();
			this.declaredArtifact = declaredArtifact;
		}
		
		private Reason initArtifactReason() {
			ArtifactCompilationFailed reason = TemplateReasons.build(ArtifactCompilationFailed.T)
																.assign(ArtifactCompilationFailed::setArtifact, compiledArtifactIdentification)
																.toReason();
			compiledArtifact.setWhyInvalid(reason);
			compiledArtifact.setInvalid(true);
			return reason;
		}
		
		private Reason initDependencyManagementCompilationFailure() {
			DependencyManagementCompilationFailed reason = TemplateReasons.build(DependencyManagementCompilationFailed.T) //
					.assign(DependencyManagementCompilationFailed::setArtifact, compiledArtifactIdentification) //
					.toReason();
			compiledArtifact.setDependencyManagementFailure(reason);
			return reason;
		}
		
		private void addDependencyManagementCompilationFailure(Reason reason) {
			dependencyManagementCompilationFailure.get().getReasons().add(reason);
		}

		public Maybe<CompiledArtifact> compile() {
			if (declaredArtifact == null) {
				Maybe<DeclaredArtifact> declaredArtifactMaybe = declaredArtifactResolver.resolve(compiledArtifactIdentification);
				
				if (declaredArtifactMaybe.isUnsatisfied())
					return declaredArtifactMaybe.whyUnsatisfied().asMaybe();
				
				declaredArtifact = declaredArtifactMaybe.get();
			}
			
			compiledArtifact.setOrigin(declaredArtifact);
			
			MinimalArtifactCompiling minimalCompiling = new MinimalArtifactCompiling(declaredArtifact, compiledArtifact);
			
			resolveParent(minimalCompiling);
			
			// transfer identification
			Maybe<CompiledArtifactIdentification> identificationMaybe = minimalCompiling.getIdentification();
			
			CompiledArtifactIdentification identification = null;

			if (!identificationMaybe.isEmpty()) {
				identification = identificationMaybe.value();
			
			if (compiledArtifactIdentification == null) {
				compiledArtifactIdentification = identification;
			}
			else {
				if (compiledArtifactIdentification.compareTo(identification) != 0) {
					return TemplateReasons.build(ArtifactIdentificationMismatch.T) //
							.assign(ArtifactIdentificationMismatch::setAddress, compiledArtifactIdentification) //
							.assign(ArtifactIdentificationMismatch::setArtifact, identification) //
							.cause(identificationMaybe.whyUnsatisfied()).toMaybe();
				}
			}
			}
			
			if (identificationMaybe.isUnsatisfied()) {
				return TemplateReasons.build(MalformedArtifactDescriptor.T).assign(MalformedArtifactDescriptor::setArtifact, compiledArtifactIdentification).cause(identificationMaybe.whyUnsatisfied()).toMaybe();
			}
			
			compiledArtifact.setGroupId(compiledArtifactIdentification.getGroupId());
			compiledArtifact.setArtifactId(compiledArtifactIdentification.getArtifactId());
			compiledArtifact.setVersion(compiledArtifactIdentification.getVersion());
			
			// check parent error
			if (compiledArtifact.getInvalid()) {
				return Maybe.complete(compiledArtifact);
			}

			// check for parent and identification problems and return eagerly
			
			// initialize preevaluated artifact
			StatefulPropertyResolution spr = new StatefulPropertyResolution(compiledArtifact);
			DeclaredArtifact preEvaluatedArtifact = spr.cloneEntity(declaredArtifact);
			compiledArtifact.setPreEvaluatedArtifact(preEvaluatedArtifact);
					
			initAggregatedArtifact();
			
			aggregateParentChain(compiledArtifact);
			
			buildEffectiveArtifact();
			
			compileDescription();
			
			compileProperties();

			compileManagedDependencies();
			
			compileDependencies();
			
			return Maybe.complete(compiledArtifact);
		}
		
		private void resolveParent(MinimalArtifactCompiling minimalCompiling) {
			Maybe<CompiledDependencyIdentification> parentDependencyMaybe = minimalCompiling.getParentDependency();
			
			if (parentDependencyMaybe.isUnsatisfied()) {
				if (!parentDependencyMaybe.isUnsatisfiedBy(NotFound.T)) {
					addFailure(TemplateReasons.build(InvalidParentReference.T).cause(parentDependencyMaybe.whyUnsatisfied()).toReason());
					return;
				}
			}
			else {
				CompiledDependencyIdentification parentDependency = parentDependencyMaybe.get();
				compiledArtifact.setParent(parentDependency);
				
				CompiledSolution parentSolution = CompiledSolution.T.create();
				parentSolution.setDependency(parentDependency);
				
				compiledArtifact.setParentSolution(parentSolution);
				
				Maybe<CompiledArtifactIdentification> parentCaiMaybe = dependencyResolver.resolveDependency(parentDependency);
				
				if (parentCaiMaybe.isUnsatisfied()) {
					parentSolution.setFailure(parentCaiMaybe.whyUnsatisfied());
					addFailure(TemplateReasons.build(UnresolvedParent.T).assign(UnresolvedParent::setDependency, parentDependency).cause(parentCaiMaybe.whyUnsatisfied()).toReason());
					return;
				}
				
				CompiledArtifactIdentification parentCai = parentCaiMaybe.get();
				
				Maybe<CompiledArtifact> parentMaybe = resolveCompiledArtifactCycleAware(parentCai);
				
				if (parentMaybe.isUnsatisfied()) {
					parentSolution.setFailure(parentMaybe.whyUnsatisfied());
					addFailure(TemplateReasons.build(UnresolvedParent.T).assign(UnresolvedParent::setDependency, parentDependency).cause(parentMaybe.whyUnsatisfied()).toReason());
					return;
				}
				
				CompiledArtifact parent = parentMaybe.get();
				parentSolution.setSolution(parent);
				
				if (parent.getInvalid()) {
					addFailure(TemplateReasons.build(InvalidParent.T).assign(InvalidParent::setArtifact, parentCai).cause(parent.getWhyInvalid()).toReason());
					return;
				}
			}
		}
		
		private void compileDescription() {
			compiledArtifact.setPackaging(Optional.ofNullable(effectiveDeclaredArtifact.getPackaging()).orElse("jar"));
			compiledArtifact.setArchetype(effectiveDeclaredArtifact.getProperties().get("archetype"));
			
			DistributionManagement distributionManagement = effectiveDeclaredArtifact.getDistributionManagement();
			if (distributionManagement != null) {
				Relocation relocation = distributionManagement.getRelocation();			
				// TODO : get rid of that ugly .. only used to complete relocations
				if (relocation != null) {
					Relocation sanitizedRelocation = Relocation.from(relocation, VersionedArtifactIdentification.create( compiledArtifact.getGroupId(), compiledArtifact.getArtifactId(), compiledArtifact.getVersion().asString()));			
					compiledArtifact.setRelocation(sanitizedRelocation);
				}
			}		
		}

		private void compileProperties() {
			Map<String,String> properties = compiledArtifact.getProperties();
			
			properties.putAll(effectiveDeclaredArtifact.getProperties());
			
			for (Map.Entry<String, String> entry : properties.entrySet()) {
				@SuppressWarnings("unchecked")
				ExpressivePropertyMapping<Object,Object> expressivePropertyMapping = (ExpressivePropertyMapping<Object, Object>) expressivePropertyMappings.get(entry.getKey());
				if (expressivePropertyMapping == null) {
					continue;
				}
				StringReader reader = new StringReader( entry.getValue());
				GmDeserializationOptions options = GmDeserializationOptions.defaultOptions.derive().setInferredRootType( expressivePropertyMapping.getDeclaredType()).set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic).build();
				Object propertyValue = marshaller.unmarshall(reader, options);
				Function<Object, Object> transformerFunction = expressivePropertyMapping.getTransformerFunction();
				if (transformerFunction != null) {
					propertyValue = transformerFunction.apply( propertyValue);
				}
				expressivePropertyMapping.getProperty().set(compiledArtifact, propertyValue);					
			}
		}

		private void addFailure(Reason reason) {
			artifactFailure.get().getReasons().add(reason);
		}
		
		private void compileManagedDependencies() {
			int i = 0;
			for (DeclaredDependency managedDependency: effectiveDeclaredArtifact.getManagedDependencies()) {
				CompiledArtifact declaringParent = managedDependencyDeclarators.get(i++);
				
				if (!"import".equals(managedDependency.getScope())) {
					CompiledDependency compiledManagedDependency = CompiledDependency.from(managedDependency);
					
					// requires version (other must be there, no check needed), if fails -> invalidate
					if ( compiledManagedDependency.getVersion() == null) {
						compiledManagedDependency.setInvalid(true);
						String msg = "Malformed dependency, version undefined : " + compiledManagedDependency.asString();
						Reason invalidationReason = Reasons.build(MalformedDependency.T).text(msg).toReason();
						compiledManagedDependency.setWhyInvalid( invalidationReason);
					}
					
					registerManagedDependency(compiledManagedDependency);
					continue;
				}
				
				CompiledDependencyIdentification importCdi = CompiledDependencyIdentification.from(managedDependency);

				ImportSolution importSolution = ImportSolution.T.create();
				importSolution.setDependency(importCdi);
				importSolution.setDeclaringParent(declaringParent);
				
				compiledArtifact.getImportSolutions().add(importSolution);
				if (declaringParent == compiledArtifact)
				compiledArtifact.getImports().add(importCdi);
				
				Maybe<CompiledArtifactIdentification> importCaiMaybe = dependencyResolver.resolveDependency(importCdi);
				
				if (importCaiMaybe.isUnsatisfied()) {
					importSolution.setFailure(importCaiMaybe.whyUnsatisfied());
					addDependencyManagementCompilationFailure(TemplateReasons.build(UnresolvedImport.T) //
						.enrich(r -> {
							r.setDependency(importCdi); //
							r.setArtifact(CompiledArtifactIdentification.from(declaringParent)); //
						}).cause(importCaiMaybe.whyUnsatisfied()).toReason()); //
					continue;
				}
				
				CompiledArtifactIdentification importCai = importCaiMaybe.get();
				
				Maybe<CompiledArtifact> importArtifactMaybe = resolveCompiledArtifactCycleAware(importCai);

				if (importArtifactMaybe.isUnsatisfied()) {
					importSolution.setFailure(importArtifactMaybe.whyUnsatisfied());
					UnresolvedImport reason = TemplateReasons.build(UnresolvedImport.T).enrich(r -> { //
						r.setDependency(importCdi); //
						r.setArtifact(CompiledArtifactIdentification.from(declaringParent)); //
					}).cause(importArtifactMaybe.whyUnsatisfied()).toReason(); //
					addDependencyManagementCompilationFailure(reason);
					continue;
				}
				
				CompiledArtifact importArtifact = importArtifactMaybe.get();

				importSolution.setSolution(importArtifact);
				
				if (importArtifact.getInvalid()) {
					addDependencyManagementCompilationFailure(TemplateReasons.build(InvalidImport.T) //
							.assign(InvalidImport::setArtifact, importCai) //
							.assign(InvalidImport::setImporter, CompiledArtifactIdentification.from(declaringParent)) //
							.cause(importArtifact.getWhyInvalid()).toReason());
					continue;
				}
				
				if (importArtifact.getDependencyManagementFailure() != null) {
					addDependencyManagementCompilationFailure(TemplateReasons.build(InvalidImport.T) //
							.assign(InvalidImport::setArtifact, importCai) //
							.assign(InvalidImport::setImporter, CompiledArtifactIdentification.from(declaringParent)) //
							.cause(importArtifact.getDependencyManagementFailure()).toReason());
				}

				List<CompiledDependency> managedDependencies = importArtifact.getManagedDependencies();
				
				for (CompiledDependency importedDependency: managedDependencies) {
					registerManagedDependency(importedDependency);
				}
			}
		}
		
		private void registerManagedDependency(CompiledDependency managedDependency) {
			effectiveManagedDependencies.put(HashComparators.unversionedDeclaredDependency.eqProxy(managedDependency), managedDependency);
			managedDependencies.add(managedDependency);
		}


		private void buildEffectiveArtifact() {
			StatefulPropertyResolution propertyResolution = new StatefulPropertyResolution(aggregatedDeclaredArtifact);
			effectiveDeclaredArtifact = propertyResolution.cloneEntity(aggregatedDeclaredArtifact);
			compiledArtifact.setPropertyProblems(propertyResolution.getPropertyProblems());
		}

		private void aggregateParentChain(CompiledArtifact compiledArtifact) {
			
			CompiledSolution parentSolution = compiledArtifact.getParentSolution();
			
			if (parentSolution != null) {
				CompiledArtifact parent = parentSolution.getSolution();
				if (parent != null)
					aggregateParentChain(parent);
			}
			
			aggregate(compiledArtifact, aggregatedDeclaredArtifact);
		}

		private void initAggregatedArtifact() {
			DeclaredArtifact declaredArtifact = compiledArtifact.getPreEvaluatedArtifact();

			aggregatedDeclaredArtifact = DeclaredArtifact.T.create();
			
			aggregatedDeclaredArtifact.setGroupId(declaredArtifact.getGroupId());
			aggregatedDeclaredArtifact.setArtifactId(declaredArtifact.getArtifactId());
			aggregatedDeclaredArtifact.setVersion(declaredArtifact.getVersion());
			aggregatedDeclaredArtifact.setDescription(declaredArtifact.getDescription());
			aggregatedDeclaredArtifact.setDistributionManagement(declaredArtifact.getDistributionManagement());
			aggregatedDeclaredArtifact.setPackaging(declaredArtifact.getPackaging());
			aggregatedDeclaredArtifact.setParentReference(declaredArtifact.getParentReference());
			aggregatedDeclaredArtifact.setName(declaredArtifact.getName());
		}

		/**
		 * This method will aggregate the following collection properties of {@link DeclaredArtifact}:
		 * 
		 * <ul>
		 * <li>{@link DeclaredArtifact#getDependencies() dependencies}
		 * <li>{@link DeclaredArtifact#getManagedDependencies() managedDependencies}
		 * <li>{@link DeclaredArtifact#getLicenses() licenses}
		 * <li>{@link DeclaredArtifact#getProperties() properties}
		 * </ul> 
		 *
		 * In case of the {@link DeclaredArtifact#getManagedDependencies()} it will register a position based
		 * correlation to the declarator parent in {@link #managedDependencyDeclarators}.
		 */
		private void aggregate(CompiledArtifact sourceParent, DeclaredArtifact target) {
			DeclaredArtifact source = sourceParent.getPreEvaluatedArtifact();
			
			target.getDependencies().addAll(source.getDependencies());
			List<DeclaredDependency> sourceManagedDependencies = source.getManagedDependencies();
			target.getManagedDependencies().addAll(sourceManagedDependencies);
			int size = sourceManagedDependencies.size();
			for (int i = 0; i < size; i++) {
				managedDependencyDeclarators.add(sourceParent);
			}
			target.getProperties().putAll(source.getProperties());
			target.getLicenses().addAll(source.getLicenses());
		}
		
		private void compileDependencies() {
			// transfer dependencies
			List<CompiledDependency> dependencies = compiledArtifact.getDependencies();
			
			for (DeclaredDependency declaredDependency : effectiveDeclaredArtifact.getDependencies()) {
				CompiledDependency compiledDependency = CompiledDependency.T.create();
				
				Consumer<Reason> reasonCollector = r -> {
					Reason collatorReason = compiledDependency.getWhyInvalid();
					if (collatorReason == null) {
						String msg = "Malformed dependency " + declaredDependency.asString();
						collatorReason = Reasons.build(MalformedDependency.T).text(msg).toReason();
						compiledDependency.setWhyInvalid(collatorReason);
						compiledDependency.setInvalid(true);
					}
					collatorReason.getReasons().add(r);
				};
				
				compiledDependency.setOrigin(compiledArtifact);

				ReasonCollectingAccessor<DeclaredDependency> rca = new ReasonCollectingAccessor<>(declaredDependency, reasonCollector);
				
				rca.get(ReasonedDeclaredDependency.groupId).ifSatisfied(compiledDependency::setGroupId);
				rca.get(ReasonedDeclaredDependency.artifactId).ifSatisfied(compiledDependency::setArtifactId);
				rca.get(ReasonedDeclaredDependency.exclusions).ifSatisfied(compiledDependency::setExclusions);
				rca.get(ReasonedDeclaredDependency.classifier).ifSatisfied(compiledDependency::setClassifier);
				rca.get(ReasonedDeclaredDependency.processingInstructions).ifSatisfied(pis -> transferPis(pis, compiledDependency));
				rca.get(ReasonedDeclaredDependency.type).map(t -> t == null? "jar": t).ifSatisfied(compiledDependency::setType);
				
				Maybe<String> versionMaybe = rca.get(ReasonedDeclaredDependency.version);
				
				if (versionMaybe.isSatisfied()) {
					String versionAsStr = versionMaybe.get();
					
					if (versionAsStr != null) {
						parseVersionExpression(versionAsStr) //
							.ifSatisfied(compiledDependency::setVersion) //
							.ifUnsatisfied(reasonCollector) //
							.ifUnsatisfied(r -> compiledDependency.setVersion(emptyVersionRange()));
					}
					else {
						Maybe<CompiledDependency> managedDependencyMaybe = resolveManaged(declaredDependency);
						
						if (managedDependencyMaybe.isSatisfied()) {
							CompiledDependency managedDependency = managedDependencyMaybe.get();
							
							compiledDependency.setVersion(managedDependency.getVersion());
							// if a corresponding dependency is found, ITS scope defines the scope of the compiled dependency 
							String scopeFromManagedDependency = managedDependency.getScope();
							if (scopeFromManagedDependency != null) {
								compiledDependency.setScope(scopeFromManagedDependency);
							}
							
							if (managedDependency.getOptional()) {
								compiledDependency.setOptional(true);
							}
						}
						else {
							compiledDependency.setVersion(emptyVersionRange());
							reasonCollector.accept(managedDependencyMaybe.whyUnsatisfied());
						}
					}
				}
				else {
					compiledDependency.setVersion(emptyVersionRange());
				}
				
				// transfer of scope property with respect to explicit, managed or default value
				rca.get(ReasonedDeclaredDependency.scope) //
					.ifSatisfied(s -> {
						if (s != null) {
							compiledDependency.setScope(s);	
						}
						else if (compiledDependency.getScope() == null) {
							compiledDependency.setScope("compile");
						}
					}) //
					.ifUnsatisfied(r -> compiledDependency.setScope("<n/a>"));

				// transfer of optional property with respect to explicit, managed or default value
				rca.get(ReasonedDeclaredDependency.optional) //
					.ifSatisfied(o -> {
						if (o != null) {
							compiledDependency.setOptional(o);
						}
					});
				
				dependencies.add(compiledDependency);
			}
		}
		
		/**
		 * resolves the version of an dependency via dependency management
		 * @param declaredDependency - the {@link DeclaredDependency} 
		 * @return - the version expression for that dependency or null
		 */
		private Maybe<CompiledDependency> resolveManaged(DeclaredDependency declaredDependency) {
			if (dependencyManagementCompilationFailure.isInitialized()) {
				return Maybe.empty(dependencyManagementCompilationFailure.get());
			}
			CompiledDependency compiledDependency = effectiveManagedDependencies.get( HashComparators.unversionedDeclaredDependency.eqProxy(declaredDependency));
			
			if (compiledDependency != null) {
				if (compiledDependency.getInvalid()) {
					return Reasons.build(MalformedManagedDependency.T) //
							.text("Invalid matching managed dependency for: " + declaredDependency.asString()) //
							.cause(compiledDependency.getWhyInvalid()) //
							.toMaybe();
				}
				
				return Maybe.complete(compiledDependency);
			}
			
			return Reasons.build(UnresolvedManagedDependency.T).text("Could not find matching managed dependency for: " + declaredDependency.asString()).toMaybe();
		}
		
	}
	
	/*
	 * private static Maybe<Version> parseVersion(String versionAsStr) { try {
	 * return Maybe.complete(Version.parse(versionAsStr)); } catch
	 * (IllegalStateException e) { return
	 * Reasons.build(ParseError.T).text(e.getMessage()).toMaybe(); } }
	 */
	
	private static Maybe<VersionExpression> parseVersionExpression(String versionExpression) {
		try {
			return Maybe.complete(VersionExpression.parse(versionExpression));
		}
		catch (IllegalStateException e) {
			return Reasons.build(ParseError.T).text(e.getMessage()).toMaybe();
		}
	}
	
	private static VersionRange emptyVersionRange() {
		Version zeroVersion = Version.create(0);
		return VersionRange.from(zeroVersion, false, zeroVersion, false);
	}
	

	private static void transferPis(List<ProcessingInstruction> pis, CompiledDependency compiledDependency) {
		if (pis != null) {
			pis.forEach(i -> transferPi(i, compiledDependency));
			compiledDependency.setProcessingInstructions( pis);
		}
	}
	private static void transferPi(ProcessingInstruction pi, CompiledDependency compiledDependency) {
		switch (pi.getTarget()) {
		case "tag":
		case "group":
			compiledDependency.getTags().add(pi.getData().trim());
			break;
		case "part":
			Pair<PartIdentification, String> partInfo = PiCommons.parsePart(pi.getData());
			compiledDependency.getParts().put(partInfo.first(), partInfo.second());
			break;
		default:
			break;
		}
	}

	private class MinimalArtifactCompiling {
		private final ArtifactPropertyResolution propertyResolution;
		private DeclaredArtifact declaredArtifact;
		private VersionedArtifactIdentification parentReference;
		
		public MinimalArtifactCompiling(DeclaredArtifact declaredArtifact, CompiledArtifact compiledArtifact) {
			this.declaredArtifact = declaredArtifact;
			this.parentReference = declaredArtifact.getParentReference();
			this.propertyResolution = new ArtifactPropertyResolution(declaredArtifact, compiledArtifact);
		}

		private Maybe<CompiledArtifactIdentification> getIdentification() {
			String groupId = NullSafe.get(declaredArtifact.getGroupId(), parentReference != null? parentReference.getGroupId(): null);
			String artifactId = declaredArtifact.getArtifactId();
			String version = NullSafe.get(declaredArtifact.getVersion(), parentReference != null? parentReference.getVersion(): null);

			LazyInitialized<InvalidArtifactIdentification> lazyReason = new LazyInitialized<>(() -> {
				return Reasons.build(InvalidArtifactIdentification.T).text("invalid artifact identification").toReason();
			});
			
			String grpid = getMaybeReasoned(propertyResolution.resolvePropertyPlaceholders(groupId), lazyReason, VersionedArtifactIdentification.groupId);
			String artId = getMaybeReasoned(propertyResolution.resolvePropertyPlaceholders(artifactId), lazyReason, VersionedArtifactIdentification.artifactId);
			String vrs = getMaybeReasoned(propertyResolution.resolvePropertyPlaceholders(version), lazyReason, VersionedArtifactIdentification.version);
			
			CompiledArtifactIdentification ai = CompiledArtifactIdentification.create(grpid, artId, vrs);
			if (lazyReason.isInitialized())
				return Maybe.incomplete(ai, lazyReason.get());
			
			return Maybe.complete(ai);
		}
		
		private Maybe<CompiledDependencyIdentification> getParentDependency() {
			if (parentReference == null)
				return Reasons.build(NotFound.T).toMaybe();
			
			LazyInitialized<InvalidParentReference> lazyReason = new LazyInitialized<>(() -> {
				return Reasons.build(InvalidParentReference.T).text("invalid parent reference").toReason();
			});
			
			String grpid = getMaybeReasoned(propertyResolution.resolvePropertyPlaceholders(parentReference.getGroupId()), lazyReason, VersionedArtifactIdentification.groupId);
			String artId = getMaybeReasoned(propertyResolution.resolvePropertyPlaceholders(parentReference.getArtifactId()), lazyReason, VersionedArtifactIdentification.artifactId);
			String vrs = getMaybeReasoned(propertyResolution.resolvePropertyPlaceholders(parentReference.getVersion()), lazyReason, VersionedArtifactIdentification.version);
			
			if (lazyReason.isInitialized())
				return lazyReason.get().asMaybe();
			
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.create(grpid, artId, vrs);
			return Maybe.complete(cdi);
		}
		
	}
	
	private static <T> T getMaybeReasoned(Maybe<T> maybe, LazyInitialized<? extends Reason> lazyCollectorReason, String property) {
		if (maybe.isUnsatisfied()) {
			lazyCollectorReason.get().getReasons().add(maybe.whyUnsatisfied());
			return null;
		}
		
		if (maybe.get() == null) {
			lazyCollectorReason.get().getReasons().add(Reasons.build(NotFound.T).text(property + " is undefined.").toReason());
			return null;
		}
		
		return maybe.get();
	}
	
	@Override
	public Maybe<CompiledArtifact> compileReasoned(DeclaredArtifact declaredArtifact) {
		return new StatefulArtifactCompiler(declaredArtifact).compile();
	}
	
	@Override
	public Maybe<CompiledArtifact> compileReasoned(File file) {
		if (!file.exists()) {
			return Reasons.build(NotFound.T).text("File " + file.getAbsolutePath() + " does not exist").toMaybe();
		}
		
		return DeclaredArtifactIdentificationExtractor.readArtifact(() -> new FileInputStream(file) , file.getAbsolutePath()) //
			.flatMap(this::compileReasoned);
	}
	
	@Override
	public Maybe<CompiledArtifact> compileReasoned(InputStream in) {
		return DeclaredArtifactIdentificationExtractor.readArtifact(in, null) //
				.flatMap(this::compileReasoned);
	}
}
