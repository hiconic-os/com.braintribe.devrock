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
package com.braintribe.devrock.mc.core.repository.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.repository.local.ArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.api.repository.local.PartAvailability;
import com.braintribe.devrock.mc.api.repository.local.PartAvailabilityAccess;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.core.commons.Downloads;
import com.braintribe.devrock.mc.core.commons.FileCommons;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Snapshot;
import com.braintribe.model.artifact.maven.meta.SnapshotVersion;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.version.Version;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * a {@link PartAvailabilityAccess} for SNAPSHOT artifacts
 * @author pit
 *
 */
public class SnapshotPartAvailabilityAccess extends AbstractPartAvailabilityAccess {
	
	private static DeclaredMavenMetaDataMarshaller marshaller = new DeclaredMavenMetaDataMarshaller();
	private final LazyInitialized<SnapshotInfo> snapshotInfo = new LazyInitialized<>( this::initializeSnapshotInfo);
	
	
	private static class SnapshotInfo {
		Version version;
		Set<EqProxy<PartIdentification>> parts;
	}
	
	/**
	 * @param compiledArtifactIdentification - the full monty artifact
	 * @param lockSupplier - a {@link Function} that returns the {@link ReadWriteLock} for a specified file 
	 * @param localRepository - the path to the local repostory's root 
	 */
	public SnapshotPartAvailabilityAccess(CompiledArtifactIdentification compiledArtifactIdentification,
			Function<File, ReadWriteLock> lockSupplier, ArtifactFilterExpert artifactFilter,
			File localRepository, Repository repository, ArtifactPartResolverPersistenceDelegate repoDelegate) {
		super(compiledArtifactIdentification, lockSupplier, artifactFilter, localRepository, repository, repoDelegate);				
	}

	
	@Override
	protected PartAvailability getAvailability(CompiledPartIdentification cpi) {		
		SnapshotInfo snapshots = snapshotInfo.get();
		PartAvailability partAvailability = snapshots.parts.contains( HashComparators.partIdentification.eqProxy(cpi)) ? PartAvailability.available : PartAvailability.unavailable;
		return partAvailability;
	}
	
	@Override
	public void setAvailablity(PartIdentification partIdentification, PartAvailability availablity) {
		throw new UnsupportedOperationException("unexpected call");		
	}

	
	/**
	 * @param file - the file to load 
	 * @return - the read {@link MavenMetaData}
	 */
	private MavenMetaData load( File file ) {
		return load( () -> new FileInputStream(file));
	}
	
	/**
	 * @param inputstreamProvider - the {@link InputStreamProvider} that provides the input stream
	 * @return - the read {@link MavenMetaData}
	 */
	private MavenMetaData load( InputStreamProvider inputstreamProvider) {
		try (InputStream in = inputstreamProvider.openInputStream()){
			MavenMetaData md = (MavenMetaData) marshaller.unmarshall( in);
			return md;			
		}			
		catch (Exception e) {
			throw Exceptions.unchecked(e, "error loading maven-metadata [" + compiledArtifactIdentification.asString() + "] of repo [" + repository.getName() + "]");
		}
	
	}
	
	
	/**
	 * @return - the currently active maven metadata file of the snapshot
	 */
	private MavenMetaData loadSnapshotMetadata() {
		File metadataFile = ArtifactAddressBuilder.build().root( localRepository.getAbsolutePath()).compiledArtifact(compiledArtifactIdentification).metaData(repository.getName()).toPath().toFile();
		ReadWriteLock lock = lockSupplier.apply( metadataFile);
		
		// check whether the file needs to be updated, or simply load it 
		Lock readLock = lock.readLock();				
		readLock.lock();
			
		try {
			if (metadataFile.exists()) {				
				Duration d = repoDelegate.updateInterval();
				// TODO : use reflecting method, check repository's RH capability 
				if (repoDelegate.isOffline() || !FileCommons.requiresUpdate(metadataFile, d, repoDelegate.isDynamic())) {					
					return load( metadataFile);
				}
				else {
					metadataFile.delete();
				}
			}
		}
		finally {
			readLock.unlock();
		}
		
		// file needs to be updated 
		Lock writeLock = lock.writeLock();
		writeLock.lock();
		try {				
			// 
			Maybe<ArtifactDataResolution> resolutionMaybe = repoDelegate.resolver().resolveMetadata(compiledArtifactIdentification);
			
			if (resolutionMaybe.isUnsatisfiedBy(NotFound.T))
				return null;
			
			ArtifactDataResolution resolveMetadata = resolutionMaybe.get();
			
			Reason reason = Downloads.downloadReasoned( metadataFile, resolveMetadata.getResource());
			
			if (reason == null)
				return load(metadataFile);
			
			if (reason instanceof NotFound)
				return null;
			
			throw new ReasonException(reason);
		}
		finally {
			writeLock.unlock();
		}		
		
	}
	
	/**
	 * @return - a fully qualified {@link SnapshotInfo} that reflects the snapshot
	 */
	private SnapshotInfo initializeSnapshotInfo() {
		SnapshotInfo snapshotInfo = new SnapshotInfo();
		Set<EqProxy<PartIdentification>> result = new HashSet<>();
		snapshotInfo.parts = result;
		
		MavenMetaData md = loadSnapshotMetadata();
		if (md != null) {
		
			Versioning versioning = md.getVersioning();
			if (versioning == null) {					
				throw new IllegalStateException("maven-metadata is not valid for a snapshot ");
			}
			
			Snapshot snapshot = versioning.getSnapshot();
			if (snapshot == null) {
				throw new IllegalStateException("maven-metadata is not valid for a snapshot ");
			}
			String timestamp = snapshot.getTimestamp();
			int buildNumber = snapshot.getBuildNumber();
			
			boolean localCopy = snapshot.getLocalCopy();
			
			if (!localCopy) {
			
				Version snapshotVersion = md.getVersion().copy();
				snapshotVersion.setQualifier( timestamp);
				snapshotVersion.setBuildNumber(buildNumber);
				
				snapshotInfo.version = snapshotVersion;
				
				String matchingKey = snapshotVersion.asString();
				
				
				for (SnapshotVersion sVersion : versioning.getSnapshotVersions()) {
					String value = sVersion.getValue();
					if (value.equalsIgnoreCase( matchingKey)) {
						String extension = sVersion.getExtension();
						String classifier = sVersion.getClassifier();
						
						PartIdentification partIdentification = PartIdentification.create(classifier, extension);
						result.add( HashComparators.partIdentification.eqProxy(partIdentification));										
					}
				}			
			}
			else {
				snapshotInfo.version = md.getVersion().copy();
				for (SnapshotVersion sVersion : versioning.getSnapshotVersions()) {									
					String extension = sVersion.getExtension();
					String classifier = sVersion.getClassifier();
						
					PartIdentification partIdentification = PartIdentification.create(classifier, extension);
					result.add( HashComparators.partIdentification.eqProxy(partIdentification));															
				}			
			}
		}
		return snapshotInfo;														
	}


	@Override
	public Version getActualVersion() {	
		return snapshotInfo.get().version;
	}


	@Override
	public Set<CompiledPartIdentification> getAvailableParts() {
		SnapshotInfo snapInfo = snapshotInfo.get();
		CompiledArtifactIdentification cai = compiledArtifactIdentification;
		cai.setVersion( snapInfo.version);
		
		Set<CompiledPartIdentification> result = new HashSet<>();
		
		for (EqProxy<PartIdentification> pi : snapInfo.parts) {
			result.add( CompiledPartIdentification.from(cai, pi.get()));
		}
		
		return result;
	}

	
	
}
