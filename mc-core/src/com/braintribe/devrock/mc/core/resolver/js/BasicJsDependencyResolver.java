// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.devrock.mc.core.resolver.js;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.download.PartDownloadManager;
import com.braintribe.devrock.mc.api.download.PartDownloadScope;
import com.braintribe.devrock.mc.api.js.JsDependencyResolver;
import com.braintribe.devrock.mc.api.js.JsResolutionContext;
import com.braintribe.devrock.mc.api.js.NormalizedJsEnrichment;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.AsyncCompletionMonitor;
import com.braintribe.devrock.mc.core.resolver.common.AnalysisArtifactResolutionPreparation;
import com.braintribe.devrock.model.mc.reason.UnresolvedPart;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.Promise;

public class BasicJsDependencyResolver implements JsDependencyResolver, JsResolutionConstants {

	private static final String TYPE_JS_ZIP = "js.zip";
	private static PartIdentification minPartIdentification = PartIdentification.create("min", TYPE_JS_ZIP);
	private static PartIdentification prettyPartIdentification = PartIdentification.create(TYPE_JS_ZIP);
	
	private RepositoryReflection repositoryReflection; 
	private TransitiveDependencyResolver transitiveDependencyResolver;
	private PartDownloadManager partDownloadManager;
	
	@Configurable
	@Required
	public void setRepositoryReflection(RepositoryReflection repositoryReflection) {
		this.repositoryReflection = repositoryReflection;
	}
	
	@Configurable
	@Required
	public void setPartDownloadManager(PartDownloadManager partDownloadManager) {
		this.partDownloadManager = partDownloadManager;
	}
	
	@Configurable
	@Required
	public void setTransitiveDependencyResolver(TransitiveDependencyResolver transitiveDependencyResolver) {
		this.transitiveDependencyResolver = transitiveDependencyResolver;
	}
	
	@Override
	public AnalysisArtifactResolution resolve(JsResolutionContext context, Iterable<? extends CompiledTerminal> terminals) {
		return new StatefulJsDependencyResolver(context, terminals).resolve();
	}
	
	private static class ArtifactEnrichmentInfo {
		AnalysisArtifact artifact;
		
		public ArtifactEnrichmentInfo(AnalysisArtifact artifact) {
			super();
			this.artifact = artifact;
		}
		
		Promise<Maybe<ArtifactDataResolution>> minPromise;
		Promise<Maybe<ArtifactDataResolution>> prettyPromise;
	}
	
	private class StatefulJsDependencyResolver {
		private final JsResolutionContext context;
		private final Iterable<? extends CompiledTerminal> terminals;
		private final PartDownloadScope downloadScope = partDownloadManager.openDownloadScope();
		private final List<Reason> occurredErrors = new ArrayList<>();

		public StatefulJsDependencyResolver(JsResolutionContext context, Iterable<? extends CompiledTerminal> terminals) {
			super();
			this.context = context;
			this.terminals = terminals;
		}
		
		public AnalysisArtifactResolution resolve() {
			TransitiveResolutionContext transitiveContext = TransitiveResolutionContext.build() //
					.lenient(true) //
					.artifactFilter(this::filterArtifact) //
					.dependencyFilter(this::filterDependency).done();
			
			AnalysisArtifactResolution artifactResolution = transitiveDependencyResolver.resolve(transitiveContext, terminals);
			
			List<AnalysisArtifact> libraryArtifacts = artifactResolution.getSolutions().stream()
					.filter(a -> !"pom".equals(a.getOrigin().getPackaging()))
					.collect(Collectors.toList());
			
			// enrichment according to the resolution context
			if (context.enrichMin() || context.enrichPretty() || context.enrichNormalized() != NormalizedJsEnrichment.none) {
				// explicit part enrichment
				List<ArtifactEnrichmentInfo> enrichmentInfos = new ArrayList<>();
				
				for (AnalysisArtifact artifact: libraryArtifacts) {
					Part pomPart = artifact.getParts().get(PartIdentifications.pomPartKey);
					
					if (repositoryReflection.isCodebase(pomPart.getRepositoryOrigin()))
						continue;
					
					ArtifactEnrichmentInfo enrichmentInfo = new ArtifactEnrichmentInfo(artifact);
					
					if (context.enrichMin())
						enrichmentInfo.minPromise = download(artifact, minPartIdentification);
					
					if (context.enrichPretty())
						enrichmentInfo.prettyPromise = download(artifact, prettyPartIdentification);
					
					enrichmentInfos.add(enrichmentInfo);
				}
				
				for (ArtifactEnrichmentInfo enrichmentInfo: enrichmentInfos) {
					AnalysisArtifact artifact = enrichmentInfo.artifact;
					
					linkPartFromPromise(artifact, enrichmentInfo.minPromise, minPartIdentification);
					linkPartFromPromise(artifact, enrichmentInfo.prettyPromise, prettyPartIdentification);
				}
				
				// normalized part enrichment
				if (context.enrichNormalized() != NormalizedJsEnrichment.none) {
					AsyncCompletionMonitor completionMonitor = new AsyncCompletionMonitor();
					
					final PartIdentification primaryPartIdentification;
					final PartIdentification fallbackPartIdentification;
					final boolean primaryPartIsExplicitlyEnriched;
					final boolean fallbackPartIsExplicitlyEnriched;
					
					if (context.enrichNormalized() == NormalizedJsEnrichment.preferMin) {
						primaryPartIdentification = minPartIdentification;
						fallbackPartIdentification = prettyPartIdentification;
						primaryPartIsExplicitlyEnriched = context.enrichMin();
						fallbackPartIsExplicitlyEnriched = context.enrichPretty();
					}
					else {
						primaryPartIdentification = prettyPartIdentification;
						fallbackPartIdentification = minPartIdentification;
						primaryPartIsExplicitlyEnriched = context.enrichPretty();
						fallbackPartIsExplicitlyEnriched = context.enrichMin();
					}
					
					for (ArtifactEnrichmentInfo enrichmentInfo: enrichmentInfos) {
						ensureNormalizedPart(completionMonitor, enrichmentInfo.artifact, primaryPartIdentification, primaryPartIsExplicitlyEnriched, fallbackPartIdentification, fallbackPartIsExplicitlyEnriched);
					}
					
					completionMonitor.waitForCompletion();
				}
			}		
			
			// Error collating
			if (!occurredErrors.isEmpty()) {
				Reason collatorReason = AnalysisArtifactResolutionPreparation.acquireCollatorReason(artifactResolution);
				collatorReason.getReasons().addAll(occurredErrors);
			}

			// check whether to throw an exception 
			if (!context.lenient() && artifactResolution.getFailure() != null) {
				throw new IllegalStateException(artifactResolution.getFailure().stringify());
			}
			
			// exclude aggregators on demand
			if (!context.includeAggregatorsInSolutions()) {
				artifactResolution.setSolutions(libraryArtifacts);
			}
			
			return artifactResolution;
		}
		
		private void addError(Reason reason) {
			synchronized (occurredErrors) {
				occurredErrors.add(reason);
			}
		}
		
		private Promise<Maybe<ArtifactDataResolution>> download(AnalysisArtifact artifact, PartIdentification partIdentification) {
			Promise<Maybe<ArtifactDataResolution>> promise = downloadScope.download(artifact.getOrigin(), partIdentification);
			
			promise.get(AsyncCallback.of((Maybe<ArtifactDataResolution> optional) -> {
				if (optional.isSatisfied()) {
					context.listener().onArtifactEnriched(artifact, partIdentification);
				}
			}, e -> {
				/* ignored */
			}));
			
			return promise;
		}
		
		private void ensureNormalizedPart(AsyncCompletionMonitor completionMonitor, AnalysisArtifact artifact, 
				PartIdentification primaryPartIdentification, boolean primaryPartIsExplicitlyEnriched, PartIdentification fallbackPartIdentification, boolean fallbackPartIsExplicitlyEnriched) {

			linkPart(completionMonitor, artifact, primaryPartIdentification, primaryPartIsExplicitlyEnriched, () -> {
				linkPart(completionMonitor, artifact, fallbackPartIdentification, fallbackPartIsExplicitlyEnriched, () -> {
					Reason collatorReason = AnalysisArtifactResolutionPreparation.acquireCollatorReason(artifact);
					
					Reason cause = Reasons.build(NotFound.T).text("Could neither resolve part [" + primaryPartIdentification.asString()  + "] nor part [" + 
							fallbackPartIdentification.asString() + "] for artifact " + artifact.asString()).toReason();

					Reason reason = TemplateReasons.build(UnresolvedPart.T).enrich(r -> r.setPart(PartIdentification.from(primaryPartIdentification))).cause(cause).toReason();
					
					addError(reason);
					collatorReason.getReasons().add(reason);
				});
			});
		}
		
		
		private void linkPart(AsyncCompletionMonitor completionMonitor, AnalysisArtifact artifact,
				PartIdentification partIdentification, boolean partIsExplicityEnriched, Runnable fallback) {
			Map<String, Part> parts = artifact.getParts();
			
			final Part part = parts.get(partIdentification.asString()); 
			
			if (part != null) {
				parts.put(PART_KEY_JS, part);
				return;
			}
			
			// should we try download primary
			if (!partIsExplicityEnriched) {
				completionMonitor.incAsync();
				Promise<Maybe<ArtifactDataResolution>> promise = download(artifact, partIdentification);
				
				promise.get(new AsyncCallback<Maybe<ArtifactDataResolution>>() {
					@Override
					public void onSuccess(Maybe<ArtifactDataResolution> o) {
						try {
							if (o.isSatisfied()) {
								ArtifactDataResolution dataResolution = o.get();
								Part downloadedPart = buildPart(partIdentification, dataResolution);
								parts.put(PART_KEY_JS, downloadedPart);
							} else {
								if (o.isUnsatisfiedBy(NotFound.T))
									fallback.run();
								else {
									throw new ReasonException(o.whyUnsatisfied());
								}
							}
						} finally {
							completionMonitor.decAsync();
						}
					}
					
					@Override
					public void onFailure(Throwable e) {
						try {
							Reason collatorReason = AnalysisArtifactResolutionPreparation.acquireCollatorReason(artifact);
							collatorReason.getReasons().add(InternalError.from(e, "Error while resolving part " + partIdentification.asString()));
						}
						catch (Exception e1) {
							e.printStackTrace();
						}
						finally {
						 completionMonitor.decAsync();
						}
					}
				});
			}
			else {
				fallback.run();
			}
		}

		private Part linkPartFromPromise(AnalysisArtifact artifact, Promise<Maybe<ArtifactDataResolution>> minPromise, PartIdentification partIdentification) {
			Part part = partFromPromise(minPromise, partIdentification);
			
			if (part != null) {
				artifact.getParts().put(partIdentification.asString(), part);
			}
			
			return part;
		}
		
		private Part partFromPromise(Promise<Maybe<ArtifactDataResolution>> minPromise, PartIdentification partIdentification) {
			if (minPromise != null) {
				Maybe<ArtifactDataResolution> optionalResolution = minPromise.get();
				
				if (optionalResolution.isUnsatisfiedBy(NotFound.T))
					return null;
				
				ArtifactDataResolution dataResolution = optionalResolution.get();
				return buildPart(partIdentification, dataResolution);
			}
			
			return null;
		}
		
		private Part buildPart(PartIdentification partIdentification, ArtifactDataResolution dataResolution) {
			Part part = Part.T.create();
			part.setResource(dataResolution.getResource());
			part.setRepositoryOrigin(dataResolution.repositoryId());
			part.setClassifier(partIdentification.getClassifier());
			part.setType(partIdentification.getType());
			
			return part;
		}
	
		private boolean filterArtifact(AnalysisArtifact artifact) {
			context.listener().onArtifactResolved(artifact);
			return true;
		}
		
		private boolean filterDependency(AnalysisDependency dependency) {
			AnalysisArtifact depender = dependency.getDepender();
			String scope = Optional.ofNullable(dependency.getScope()).orElse("compile");
			
			switch (scope) {
			case "provided":
			case "test":
				return false;
			default:
				break;
			}
			
			if (dependency.getOptional())
				return false;
			
			String type = Optional.ofNullable(dependency.getType()).orElse("jar");
			String classifier = dependency.getClassifier();
			
			if (classifier != null)
				return false;
			
			switch (type) {
			case "pom":
			case "jar":
				break;
			default:
				return false;
			}

			// We want pure JS artifacts, models and js-interopped Java artifacts to pass and regular Java artifacts filtered away.
			// Terminal is JS artifact - that's OK.

			// From there we examine dependencies, so based on depender:
			// 1. Pure JS artifact - all dependencies are OK
			// 2. API with JS-interop - dependencies marked with JS tag are OK
			// 3. Models - other models and JS-interopped artifacts are OK

			// How this impl works:
			// 1. Pure JS artifact - depender is not js-interop, so all deps pass
			// 2. APIwith JS interop - JS tag is examined
			// 3. Model - depender is not js-interop - all deps pass - TODO this is be wrong, functional deps of models might be included  
			// Maybe we should change it to (!jsinterop && !model) || hasJsTag 

			boolean jsinterop = Boolean.TRUE.toString().equals(depender.getOrigin().getProperties().get("jsinterop"));
			
			return !jsinterop || dependency.getOrigin().getTags().contains("js");
		}

	}

}
