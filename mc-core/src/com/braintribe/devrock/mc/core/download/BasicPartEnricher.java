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
package com.braintribe.devrock.mc.core.download;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.download.PartDownloadManager;
import com.braintribe.devrock.mc.api.download.PartDownloadScope;
import com.braintribe.devrock.mc.api.download.PartEnricher;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.core.resolver.common.AnalysisArtifactResolutionPreparation;
import com.braintribe.devrock.model.mc.reason.UnresolvedPart;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.consumable.PartEnrichment;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.processing.async.api.Promise;

public class BasicPartEnricher implements PartEnricher {
	private static Logger log = Logger.getLogger(BasicPartEnricher.class);
	private PartDownloadManager partDownloadManager;
	
	@Configurable @Required
	public void setPartDownloadManager(PartDownloadManager partDownloadManager) {
		this.partDownloadManager = partDownloadManager;
	}
	
	@Override
	public void enrich(PartEnrichingContext context, AnalysisArtifactResolution resolution) {
		new StateFullEnricher(context).enrich(resolution);
	}

	@Override
	public void enrich(PartEnrichingContext context, Iterable<AnalysisArtifact> solutions) {
		new StateFullEnricher(context).enrich(solutions);
	}

	private static class AsyncPartDownloadEntry {
		AnalysisArtifact artifact;
		Promise<Maybe<ArtifactDataResolution>> promise;
		PartEnrichment enrichment;
		
		public AsyncPartDownloadEntry(AnalysisArtifact artifact, PartEnrichment enrichment, Promise<Maybe<ArtifactDataResolution>> promise) {
			super();
			this.artifact = artifact;
			this.enrichment = enrichment;
			this.promise = promise;
		}
	}
	
	private class StateFullEnricher {
		private PartDownloadScope downloadScope = partDownloadManager.openDownloadScope();
		private List<AsyncPartDownloadEntry> asyncPartDownloadEntries = new ArrayList<>();
		private PartEnrichingContext context;
		
		public StateFullEnricher(PartEnrichingContext context) {
			this.context = context;
		}

		public void enrich(AnalysisArtifactResolution resolution) {
			Set<AnalysisArtifact> artifacts = collect(resolution);
			enrich(artifacts, (artifact, reason) -> {
				resolution.getIncompleteArtifacts().add(artifact);
				AnalysisArtifactResolutionPreparation.addFailure(resolution, reason);
			});
		}
		
		private Set<AnalysisArtifact> collect(AnalysisArtifactResolution resolution) {
			Set<AnalysisArtifact> artifacts = new LinkedHashSet<>();
			
			List<AnalysisArtifact> terminalArtifacts = new ArrayList<>();
			
			for (AnalysisTerminal terminal: resolution.getTerminals()) {
				if (terminal instanceof AnalysisArtifact) {
					AnalysisArtifact terminalArtifact = (AnalysisArtifact)terminal;
					collect(terminalArtifact, artifacts);
					terminalArtifacts.add(terminalArtifact);
				}
				else if (terminal instanceof AnalysisDependency)
					collect(((AnalysisDependency)terminal).getSolution(), artifacts);
			}
			
			artifacts.removeAll(terminalArtifacts);
			
			return artifacts;
		}
		
		private void collect(AnalysisArtifact analysisArtifact, Set<AnalysisArtifact> artifacts) {
			if (analysisArtifact == null)
				return;
			
			if (!artifacts.add(analysisArtifact)) 
				return;
			
			for (AnalysisDependency dependency: analysisArtifact.getDependencies()) {
				AnalysisArtifact artifact = dependency.getSolution();
				collect(artifact, artifacts);
			}
		}

		public void enrich(Iterable<AnalysisArtifact> artifacts) {
			enrich(artifacts, (a,r) -> {});
		}
		
		public void enrich(Iterable<AnalysisArtifact> artifacts, BiConsumer<AnalysisArtifact, Reason> errorListener) {
			
			for (AnalysisArtifact artifact: artifacts) {
				enrich(artifact);
			}
			
			for (AsyncPartDownloadEntry entry: asyncPartDownloadEntries) {
				Maybe<ArtifactDataResolution> optionalResolution;
				try {
					optionalResolution = entry.promise.get();
				}
				catch (Throwable e) {
					Reason reason = Reasons.build(InternalError.T).text(e.getMessage()) //
							.assign(InternalError::setJavaException, e) //
							.text("Error while resolving part [" + PartIdentification.asString(entry.enrichment) + "] for artifact [" + entry.artifact.asString() + "]") //
							.toReason();
					
					AnalysisArtifactResolutionPreparation.acquireCollatorReason(entry.artifact).getReasons().add(reason);
					
					errorListener.accept(entry.artifact, reason);
					continue;
				}
				
				// TODO: null values for a Maybe is broken - analyze why this is the case
				if (optionalResolution == null) {
					if (entry.enrichment.getMandatory()) {
						String msg = "unexpected failure during download of mandatory part [" + entry.enrichment.asString() + "] of [" + entry.artifact.toString() + "]";
						log.error(msg);
					}
					else {
						String msg = "unexpected failure during download of optional part [" + entry.enrichment.asString() + "] of [" + entry.artifact.toString() + "]";
						log.error(msg);
					}
					continue;
				}
				
				Reason unresolved = null;
				
				if (optionalResolution.isSatisfied()) {
					ArtifactDataResolution partResolution = optionalResolution.get();
					
					// TODO: establish a primitive filesystem cache in case the ArtifactDataResolution tells us that it is not locally backed as a file
					if (partResolution.isBacked()) {
						// TODO: think of async listening
						Part part = Part.T.create();
						part.setResource(partResolution.getResource());
						part.setClassifier(entry.enrichment.getClassifier());
						part.setType(entry.enrichment.getType());
						part.setRepositoryOrigin(partResolution.repositoryId());
						String key = entry.enrichment.getKey();
						if (key == null)
							key = PartIdentification.asString(entry.enrichment);
						
						entry.artifact.getParts().put(key, part);
					}
					else {
						unresolved = Reasons.build(NotFound.T).toReason();
					}
				}
				else {
					unresolved = optionalResolution.whyUnsatisfied();
				}
				
				if (unresolved != null && entry.enrichment.getMandatory()) {				
					Reason reason = Reasons.build(UnresolvedPart.T) //
							.enrich( r -> r.setPart(entry.enrichment))
							.enrich( r -> r.setArtifact( CompiledArtifactIdentification.from( entry.artifact)))
							.text("Unresolved required part [" + PartIdentification.asString(entry.enrichment) + "] for artifact [" + entry.artifact.asString() + "]") //
							.toReason();
					
					if (!(unresolved instanceof NotFound))
						reason.getReasons().add(unresolved);
					
					// add reason to artifact 
					AnalysisArtifactResolutionPreparation.acquireCollatorReason(entry.artifact).getReasons().add(reason);
					
					// notify error
					errorListener.accept(entry.artifact, reason);
				}
			}
		}
		
		private void enrich(AnalysisArtifact analysisArtifact) {
			List<PartEnrichment> enrichments = context.enrichmentExpert().apply(analysisArtifact);
			
			for (PartEnrichment enrichment: enrichments) {
				downloadPart(analysisArtifact, enrichment);
			}
		}


		private void downloadPart(AnalysisArtifact analysisArtifact, PartEnrichment enrichment) {
			Promise<Maybe<ArtifactDataResolution>> promise = downloadScope.download(CompiledArtifactIdentification.from(analysisArtifact), enrichment);
			
			AsyncPartDownloadEntry entry = new AsyncPartDownloadEntry(analysisArtifact, enrichment, promise);
			asyncPartDownloadEntries.add(entry);
		}

	}
}
