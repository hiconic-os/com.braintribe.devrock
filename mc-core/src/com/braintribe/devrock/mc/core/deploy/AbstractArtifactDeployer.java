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
package com.braintribe.devrock.mc.core.deploy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.commons.ArtifactAddress;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.deploy.ArtifactDeployer;
import com.braintribe.devrock.mc.core.commons.McConversions;
import com.braintribe.devrock.mc.core.resolver.common.AnalysisArtifactResolutionPreparation;
import com.braintribe.devrock.model.mc.reason.MetadataUploadFailed;
import com.braintribe.devrock.model.mc.reason.PartUploadFailed;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.execution.SimpleThreadPoolBuilder;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.ArtifactResolution;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.version.Version;

public abstract class AbstractArtifactDeployer<R extends Repository> implements ArtifactDeployer {
	private static Logger log = Logger.getLogger(AbstractArtifactDeployer.class);
	
	protected R repository;
	
	@Required @Configurable
	public void setRepository(R repository) {
		this.repository = repository;
	}
	
	@Override
	public ArtifactResolution deploy(Artifact artifact) {
		return deploy(Collections.singletonList(artifact));
	}
	
	class ParallelProcessing implements AutoCloseable {
		private final ExecutorService executorService;
		private List<Reason> errors;
		private final Lock reasonLock = new ReentrantLock();
		private volatile boolean errorneous;
		private final Phaser phaser = new Phaser();

		public ParallelProcessing(int threads) {
			executorService = SimpleThreadPoolBuilder.newPool().poolSize(threads, threads).workQueue(new LinkedBlockingQueue<>()).build();
		}
		
		private void addError(Reason error) {
			reasonLock.lock();
			
			try { 
				if (errors == null)
					errors = new ArrayList<>();
				
				errors.add(error);
				errorneous = true;
			}
			finally {
				reasonLock.unlock();
			}
		}
		
		public boolean execute(Runnable runnable) {
			return submit(() -> { 
				runnable.run();
				return null;
			});
		}
		
		public boolean submit(Callable<Reason> callable) {
			if (errorneous)
				return false; 
			
			phaser.register();
			
			try {
				executorService.execute(() -> {
					try {
						Reason error = callable.call();
						if (error != null)
							addError(error);
					}
					catch (Throwable e) {
						addError(InternalError.from(e));
					}
					finally {
						phaser.arrive();
					}
				});
			}
			catch (RuntimeException | Error e) {
				phaser.arrive();
			}
			
			return true;
		}
		
		
		public void await() {
			phaser.awaitAdvance(0);
		}
		
		public Reason awaitReasoned(Supplier<Reason> collectorReasonSupplier) {
			await();
			
			if (errors != null) {
				Reason reason = collectorReasonSupplier.get();
				reason.getReasons().addAll(errors);
				return reason;
			}
			
			return null;
		}
		
		@Override
		public void close() {
			executorService.shutdown();
		}
	}
	
	@Override
	public ArtifactResolution deploy(Iterable<? extends Artifact> artifacts) {
		TransferContext transferContext = openTransferContext();

		ArtifactResolution resolution = ArtifactResolution.T.create();
		
		Map<Artifact, Lock> artifactLocks = new ConcurrentHashMap<>();
		
		try (ParallelProcessing parallelProcessing = new ParallelProcessing(8)) {
			// upload parts and version level metadata in parallel 
			for (Artifact artifact: artifacts) {
				Artifact resolutionArtifact = Artifact.T.create();
				resolutionArtifact.setGroupId(artifact.getGroupId());
				resolutionArtifact.setArtifactId(artifact.getArtifactId());
				resolutionArtifact.setVersion(artifact.getVersion());
	
				resolution.getTerminals().add(resolutionArtifact);
				resolution.getSolutions().add(resolutionArtifact);
			
				MavenMetaData versionedMetaData = buildVersionedMetaData(artifact);
	
				ArtifactAddress versionedMetaDataAddress = transferContext.metaDataAddress(artifact, true);
	
				parallelProcessing.execute(() -> {
					Maybe<Resource> mdResourceMaybe = transferContext.transfer(versionedMetaDataAddress, out -> DeclaredMavenMetaDataMarshaller.INSTANCE.marshall(out, versionedMetaData));
					
					if (mdResourceMaybe.isUnsatisfied()) {
						Lock lock = artifactLocks.computeIfAbsent(resolutionArtifact, k -> new ReentrantLock());
						lock.lock();
						
						try {
							Reason error = mdResourceMaybe.whyUnsatisfied();
							Reason reason = Reasons.build(MetadataUploadFailed.T).text("Uploading artifact version metadata for " + artifact.asString() + " failed").cause(error).toReason();
							AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolutionArtifact).getReasons().add(reason);
						}
						finally {
							lock.unlock();
						}
					}
				});
				
				for (Map.Entry<String, Part> entry: artifact.getParts().entrySet()) {
					Part part = entry.getValue();
					String partKey = entry.getKey();
					Resource resource = part.getResource();
					
					final ArtifactAddress artifactAddress;
					
					if ("<escape>".equals(part.getType())) {
						artifactAddress = transferContext.newAddressBuilder().versionedArtifact(artifact).file(partKey);
					}
					else {
						artifactAddress = transferContext.newAddressBuilder().versionedArtifact(artifact).part(part);
					}
	
					parallelProcessing.execute(() -> {
						Maybe<Resource> resourcePotential = transferContext.transfer(artifactAddress, resource::writeToStream);
						
						Part resolutionPart = Part.T.create();
						resolutionPart.setClassifier(part.getClassifier());
						resolutionPart.setType(part.getType());
						
						Lock lock = artifactLocks.computeIfAbsent(resolutionArtifact, k -> new ReentrantLock());
						
						if (resourcePotential.isUnsatisfied()) {
							Reason error = resourcePotential.whyUnsatisfied();
							Reason reason = Reasons.build(PartUploadFailed.T).text("Part [" + part.asString() + "] could not be transferred.").cause(error).toReason();
							resolutionPart.setFailure(reason);
						}
						else {
							Resource uploadedResource = resourcePotential.get();
							resolutionPart.setResource(uploadedResource);
						}

						// update artifact synchronized due to parallel processing
						lock.lock();
						try {
							resolutionArtifact.getParts().put(partKey, resolutionPart);
							
							if (resolutionPart.hasFailed())
								AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolutionArtifact).getReasons().add(resolutionPart.getFailure());
						}
						finally {
							lock.unlock();
						}
					});
				}
				
				// update or create artifact metadata
				updateMetaData(transferContext, artifact);
				
			}

			// wait for all parts being uploaded
			parallelProcessing.await();
			
			// upload metadata
			for (Map.Entry<Artifact, Lock> entry: artifactLocks.entrySet()) {
				Artifact artifact = entry.getKey();
				
				// only update metadata if parts and version metadata could be uploaded
				if (artifact.hasFailed())
					continue;
				
				// update or create artifact metadata
				parallelProcessing.execute(() -> {
					Reason error = updateMetaData(transferContext, artifact);
					
					if (error != null) {
						Lock lock = artifactLocks.computeIfAbsent(artifact, k -> new ReentrantLock());
						lock.lock();
						try {
							Reason reason = Reasons.build(MetadataUploadFailed.T).text("Uploading artifact metadata for " + artifact.asString() + " failed").cause(error).toReason();
							AnalysisArtifactResolutionPreparation.acquireCollatorReason(artifact).getReasons().add(reason);
						}
						finally {
							lock.unlock();
						}
					}
				});
			}
			
			// wait for all metadata updates being done
			parallelProcessing.await();
			
			// transfer overall error
			for (Artifact artifact: artifactLocks.keySet()) {
				if (artifact.hasFailed()) {
					AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolution).getReasons().add(artifact.getFailure());
				}
			}
		}
		
		return resolution;			
	}
	
	protected abstract TransferContext openTransferContext();

	private Reason updateMetaData(TransferContext context, Artifact artifact) {
		ArtifactAddress metaDataAddress = context.metaDataAddress(artifact, false);
		
		Maybe<MavenMetaData> existingMavenMetaDataMaybe = readOrPrimeMavenMetaData(context, metaDataAddress, artifact);
		
		if (existingMavenMetaDataMaybe.isUnsatisfied())
			return existingMavenMetaDataMaybe.whyUnsatisfied();
		
		MavenMetaData existingMavenMetaData = existingMavenMetaDataMaybe.get();
		
		return updateMetaDataIfRequired(context, existingMavenMetaData, metaDataAddress, artifact);
	}

	private Reason updateMetaDataIfRequired(TransferContext context, MavenMetaData mavenMetaData,
			ArtifactAddress metaDataAddress, Artifact artifact) {
		Versioning versioning = mavenMetaData.getVersioning();
		if (versioning == null) {
			versioning = Versioning.T.create();
			mavenMetaData.setVersioning(versioning);			
		}

		Version version = Version.parse(artifact.getVersion());
		
		List<Version> versions = versioning.getVersions();
		Set<Version> sortedVersions = new TreeSet<>(versions);
		
		if (!sortedVersions.add(version))
			return null;
		
		versions.clear();
		versions.addAll(sortedVersions);		

		Date lastUpdated = new Date();
		String lastUpdatedStr = McConversions.formatMavenMetaDataDate(lastUpdated);
		versioning.setLastUpdated(lastUpdatedStr);
		versioning.setLatest(version);
		
		return context.transfer(metaDataAddress, out -> DeclaredMavenMetaDataMarshaller.INSTANCE.marshall(out, mavenMetaData)).whyUnsatisfied();
	}

	private Maybe<MavenMetaData> readOrPrimeMavenMetaData(TransferContext context, ArtifactAddress metaDataAddress, Artifact artifact) {

		Maybe<InputStream> inMaybe = context.openInputStreamReasoned(metaDataAddress);

		if (inMaybe.isUnsatisfied()) {
			if (!inMaybe.isUnsatisfiedBy(NotFound.T))
				return inMaybe.whyUnsatisfied().asMaybe();
		}
		
		if (inMaybe.isSatisfied()) {
			try (InputStream in = inMaybe.get()) {
				return DeclaredMavenMetaDataMarshaller.INSTANCE.unmarshallReasoned(in).cast();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		MavenMetaData artifactMetaData = MavenMetaData.T.create();
		artifactMetaData.setGroupId( artifact.getGroupId());
		artifactMetaData.setArtifactId( artifact.getArtifactId());
		
		Versioning versioning = Versioning.T.create();
		artifactMetaData.setVersioning(versioning);
		
		return Maybe.complete(artifactMetaData);
	}

	private MavenMetaData buildVersionedMetaData(Artifact artifact) {
		MavenMetaData versionedMetaData = MavenMetaData.T.create();
		
		versionedMetaData.setGroupId(artifact.getGroupId());
		versionedMetaData.setArtifactId(artifact.getArtifactId());
		versionedMetaData.setVersion(Version.parse(artifact.getVersion()));
		return versionedMetaData;
	}
	
	interface TransferContext {
		Maybe<Resource> transfer(ArtifactAddress address, OutputStreamer outputStreamer);
		ArtifactAddress metaDataAddress(Artifact artifact, boolean versioned);
		Maybe<InputStream> openInputStreamReasoned(ArtifactAddress address);
		Optional<InputStream> openInputStream(ArtifactAddress address);
		ArtifactAddressBuilder newAddressBuilder();
	}
}
