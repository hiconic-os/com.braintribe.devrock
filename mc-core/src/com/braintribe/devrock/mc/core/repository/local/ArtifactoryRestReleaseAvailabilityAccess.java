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
package com.braintribe.devrock.mc.core.repository.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.repository.local.PartAvailability;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.core.commons.Downloads;
import com.braintribe.devrock.mc.core.commons.FileCommons;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.devrock.model.artifactory.FileItem;
import com.braintribe.devrock.model.artifactory.FolderInfo;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * part availability access for artifactory's REST interface 
 * @author pit / dirk
 *
 */
public class ArtifactoryRestReleaseAvailabilityAccess extends AbstractPartAvailabilityAccess {
	private static JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
	private static GmDeserializationOptions options = GmDeserializationOptions.defaultOptions.derive().setInferredRootType( FolderInfo.T).build();
	private final LazyInitialized<FolderInfo> folderInfo = new LazyInitialized<>( this::loadPartOverview);


	public ArtifactoryRestReleaseAvailabilityAccess(CompiledArtifactIdentification compiledArtifactIdentification,
			Function<File, ReadWriteLock> lockProvider,
			ArtifactFilterExpert artifactFilter, File localRepository,
			Repository repository,
			BasicArtifactPartResolverPersistenceDelegate basicArtifactPartResolverPersistenceDelegate) {
		super(compiledArtifactIdentification, lockProvider, artifactFilter, localRepository, repository, basicArtifactPartResolverPersistenceDelegate);		
	}

	@Override
	public void setAvailablity(PartIdentification partIdentification, PartAvailability availablity) {
		throw new UnsupportedOperationException("unexpected call");	
	}

	@Override
	public Version getActualVersion() {
		return compiledArtifactIdentification.getVersion();
	}

	@Override
	protected PartAvailability getAvailability(CompiledPartIdentification cpi) {
		FolderInfo info = folderInfo.get();
		if (info == null) {
			return PartAvailability.unavailable;
		}
		String fileName = cpi.asFilename();
		for (FileItem item : info.getChildren()) {
			if (item.getUri().endsWith(fileName)) {
				return PartAvailability.available;
			}
		}
		return PartAvailability.unavailable;
	}

	/**
	 * retrieves (and may update) the part-availability info 
	 * @return - the {@link FolderInfo}
	 */
	private FolderInfo loadPartOverview() {
		
		File partAvailabilityFile = ArtifactAddressBuilder.build().root( localRepository.getAbsolutePath()).compiledArtifact(compiledArtifactIdentification).partAvailability(repository.getName(), "artifactory.json").toPath().toFile();
		ReadWriteLock lock = lockSupplier.apply( partAvailabilityFile);
		
		// check whether the file needs to be updated, or simply load it 
		Lock readLock = lock.readLock();				
		readLock.lock();
				
			
		try {
			if (partAvailabilityFile.exists()) {				
				Duration d = repoDelegate.updateInterval();
				// TODO : use reflecting method, check repository's RH capability				
				if (repoDelegate.isOffline() || !FileCommons.requiresUpdate(partAvailabilityFile, d, repoDelegate.isDynamic())) {					
					return load( partAvailabilityFile);
				}
				else {
					// simply delete the file  
					partAvailabilityFile.delete();
					// remove marker
					FileCommons.removeMarkerFile(lockSupplier, partAvailabilityFile);
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
			ArtifactDataResolver resolver = repoDelegate.resolver();
						
			Maybe<ArtifactDataResolution> resolutionMaybe = resolver.getPartOverview(compiledArtifactIdentification);
			
			if (resolutionMaybe.isUnsatisfiedBy(NotFound.T))
				return null;
			
			ArtifactDataResolution resolvedOverview = resolutionMaybe.get();

			Reason reason = Downloads.downloadReasoned( partAvailabilityFile, resolvedOverview::openStream);
			
			if (reason != null) {
				if (reason instanceof NotFound) {
					//create an empty part-availability-file so that it will not be tried again, and return a folderinfo 
					FolderInfo folderInfo = FolderInfo.T.create();
					folderInfo.setRepo(repository.getName());
					dump( partAvailabilityFile, folderInfo);
					return folderInfo;				
				}
				
				throw new ReasonException(reason);
			}
			
			return load( partAvailabilityFile);
		}
		finally {
			writeLock.unlock();
		}		
		
	}

	private FolderInfo load(File partAvailabilityFile) {
		try (InputStream in = new FileInputStream(partAvailabilityFile)) {
			return (FolderInfo) marshaller.unmarshall(in, options);
		}
		catch (Exception e) {
			throw Exceptions.unchecked( e, "error while unmarshalling [" + partAvailabilityFile.getAbsolutePath() + "]");
		}		
	}
	
	private void dump( File partAvailabilityFile, FolderInfo folderInfo) {
		try (OutputStream out = new FileOutputStream(partAvailabilityFile)) {
			marshaller.marshall(out, folderInfo);
		}
		catch (Exception e) {
			throw Exceptions.unchecked( e, "error while marshalling [" + partAvailabilityFile.getAbsolutePath() + "]");
		}
	}

	@Override
	public Set<CompiledPartIdentification> getAvailableParts() {
		HashSet<CompiledPartIdentification> result = new HashSet<>();
		FolderInfo fi = folderInfo.get();
		if (fi == null)
			return result;
		for (FileItem item : fi.getChildren()) {
			String fileName = item.getUri();
			if(item.getUri().startsWith("/")) {
				fileName = item.getUri().substring(1);
			}
			CompiledPartIdentification cpi = CompiledPartIdentification.fromFile(compiledArtifactIdentification, fileName);
			if (cpi != null) {
				result.add(cpi);
			}
		}
		return result;
	}
	
	
	
}
