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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.repository.local.ArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.api.repository.local.PartAvailability;
import com.braintribe.devrock.mc.api.repository.local.PartAvailabilityAccess;
import com.braintribe.devrock.mc.core.commons.FileCommons;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.LazyInitialized;


/**
 * a {@link PartAvailabilityAccess} for standard (RELEASE) artifacts
 * 
 * @author pit/dirk
 *
 */
public class ReleasePartAvailabilityAccess extends AbstractPartAvailabilityAccess {
	private LazyInitialized<ReleaseInfo> releaseInfo = new LazyInitialized<>( this::initializePartAvailability);
	
	/**
	 * struct to hold the collated information 
	 * @author pit/dirk
	 *
	 */
	private static class ReleaseInfo {
		String uuid;
		Map<EqProxy<PartIdentification>, PartAvailability> partToAvailabilityMap = new LinkedHashMap<>();		
	}
	
	/**
	 * @param compiledArtifactIdentification - the full monty artifact
	 * @param lockSupplier - a {@link Function} that returns the {@link ReadWriteLock} for a specified file 
	 * @param relevancyFilter - a {@link Predicate} that filters whether the repository reflected is relevant
	 * @param localRepository - the path to the local repostory's root 
	 * @param repositoryId - the id of the repository  
	 */
	public ReleasePartAvailabilityAccess(CompiledArtifactIdentification compiledArtifactIdentification,
			Function<File, ReadWriteLock> lockSupplier, ArtifactFilterExpert artifactFilter,
			File localRepository, Repository repository, ArtifactPartResolverPersistenceDelegate repoDelegate) {
		super(compiledArtifactIdentification, lockSupplier, artifactFilter, localRepository, repository, repoDelegate);		
	}

	
	@Override
	protected PartAvailability getAvailability(CompiledPartIdentification cpi) {		
		PartAvailability partAvailabilityDefault = repoDelegate.isOffline() ? PartAvailability.unavailable : PartAvailability.unknown;
		return releaseInfo.get().partToAvailabilityMap.getOrDefault(HashComparators.partIdentification.eqProxy(cpi), partAvailabilityDefault);
	}


	@Override
	public Version getActualVersion() {		
		return compiledArtifactIdentification.getVersion();
	}
	
	
	@Override
	public void setAvailablity(PartIdentification partIdentification, PartAvailability availablity) {
		File partAvailablilityFile = ArtifactAddressBuilder.build().root( localRepository.getAbsolutePath()).compiledArtifact(compiledArtifactIdentification).partAvailability(repository.getName()).toPath().toFile();
		ReadWriteLock lock = lockSupplier.apply( partAvailablilityFile);
		
		ReleaseInfo cachedInfo = releaseInfo.get();
		
		Lock writeLock = lock.writeLock();
		
		writeLock.lock();
		

		try {	
			EqProxy<PartIdentification> eqProxy = HashComparators.partIdentification.eqProxy(partIdentification);
			cachedInfo.partToAvailabilityMap.put( eqProxy, availablity);

			ReleaseInfo freshInfo = loadPartAvailability( partAvailablilityFile);
			if (cachedInfo.uuid.equals( freshInfo.uuid)) {
				freshInfo.partToAvailabilityMap.put( eqProxy, availablity);
				storePartAvailability(  partAvailablilityFile, freshInfo);
			}
			else {
				;
			}
		}
		finally {
			writeLock.unlock();
		}
					
	}

	/**
	 * @param partAvailabilityFile - the {@link File} to write to  
	 * @param freshInfo - the {@link ReleaseInfo} to write
	 */
	private void storePartAvailability(File partAvailabilityFile, ReleaseInfo freshInfo) {
		// if repository is currently offline, we do not store any information
		if (repository.getOffline())
			return;
		try ( Writer writer = new OutputStreamWriter(new FileOutputStream(partAvailabilityFile), "UTF-8")) {
			writer.write( freshInfo.uuid);
			for (Entry<EqProxy<PartIdentification>, PartAvailability> entry : freshInfo.partToAvailabilityMap.entrySet()) {
				writer.write( '\n');
				switch (entry.getValue()) {
					case available:
						writer.write( '+');
						break;
					case unavailable:
						writer.write( '-');
						break;
					default:
						throw new IllegalStateException("unexpected state [" + entry.getValue() + "] found");						
				}			
				writer.write(PartIdentification.asString(entry.getKey().get()));
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException( e);
		}
					
	}


	/**
	 * loads the file 
	 * @param file - the {@link File} to load 
	 * @return - the {@link ReleaseInfo} read
	 */
	private ReleaseInfo loadPartAvailability( File file) {
		// check if marker file exists:
		File markerFile = FileCommons.markerFile(file);
		boolean markerFileExists = markerFile.exists();
				
		ReleaseInfo releaseInfo = new ReleaseInfo();		
		try (BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream(file), "UTF-8"))) {
			releaseInfo.uuid = reader.readLine();
			reader.lines().forEach( l -> {		
				Pair<PartIdentification, PartAvailability> pair = digest( l);			
				if (pair != null) {
					// if a marker file exists, we have to transfer only the available parts 
					if (markerFileExists && pair.second == PartAvailability.available || !markerFileExists) {
						releaseInfo.partToAvailabilityMap.put( HashComparators.partIdentification.eqProxy(pair.first), pair.second);
					}
				}
			});		
		}
		catch (IOException e) {
			throw new UncheckedIOException( e);
		}
		
		// if file was marked as outdated, we must write the file now, and delete the marker 
		if (markerFileExists) {
			// No locking here as it's already been locked by the caller
			releaseInfo.uuid = UUID.randomUUID().toString();			
			try (BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file)))){
				writer.write( releaseInfo.uuid);				
				for (Map.Entry<EqProxy<PartIdentification>, PartAvailability> entry : releaseInfo.partToAvailabilityMap.entrySet()) {
					// only available here ..
					writer.write( "\n+" + entry.getKey().get().asString());												
				}
				
			} catch (IOException e) {
				throw new UncheckedIOException( "cannot write [" + file.getAbsolutePath() + "]", e);
			}
			
			FileCommons.removeMarkerFile(lockSupplier, file);
		}
		return releaseInfo;
	}
	

	/**
	 * digests a single expression, validates - may return null (if lenient for instance)
	 * @param l - the {@link String} to parse 
	 * @return - a {@link Pair} of {@link PartIdentification} and {@link PartAvailability}, or NULL if not parsed
	 */
	private Pair<PartIdentification, PartAvailability> digest(String l) {
		l = l.trim();
		if (l.length() == 0)
			return null;
		char c = l.charAt(0);
		PartAvailability pa = PartAvailability.unknown;
		switch (c) {
			case '+' :
				pa = PartAvailability.available;
				break;
			case '-' :
				pa = PartAvailability.unavailable;
				break;
			default:
				throw new IllegalStateException("a term [" + l + "] is not a valid expression");				
		}		
		String piAsString = l.substring(1).trim();
		PartIdentification pi = PartIdentification.parse(piAsString);
		return Pair.of( pi, pa);
	}


	/**
	 * @return - a fully instantiated {@link ReleaseInfo}, read or created
	 */
	private ReleaseInfo initializePartAvailability() {
		File partAvailablilityFile = ArtifactAddressBuilder.build().root( localRepository.getAbsolutePath()).compiledArtifact(compiledArtifactIdentification).partAvailability(repository.getName()).toPath().toFile();
		ReadWriteLock lock = lockSupplier.apply( partAvailablilityFile);
		
		// if possible, read 
		Lock readLock = lock.readLock();				
		readLock.lock();
			
		try {
			if (partAvailablilityFile.exists()) {							
				return loadPartAvailability( partAvailablilityFile);
			}
		}
		finally {
			readLock.unlock();
		}
		
		// write 
		Lock writeLock = lock.writeLock();
		writeLock.lock();
		
		try {	
			// test again, somebody might have written the file now 
			if (partAvailablilityFile.exists()) {							
				return loadPartAvailability( partAvailablilityFile);
			}
						
			// TODO: transfer the contents of the file (the parts marked with '+'			
			ReleaseInfo info = new ReleaseInfo();
			info.uuid = UUID.randomUUID().toString();			
			try {
				IOTools.spit(partAvailablilityFile, info.uuid, "UTF-8", false);
			} catch (IOException e) {
				throw new UncheckedIOException( "cannot write [" + partAvailablilityFile.getAbsolutePath() + "]", e);
			}
			return info;
		}
		finally {
			writeLock.unlock();
		}		
	}


	@Override
	public Set<CompiledPartIdentification> getAvailableParts() {
		ReleaseInfo ri = releaseInfo.get();
		Set<CompiledPartIdentification> result = new HashSet<>();
		for (Map.Entry<EqProxy<PartIdentification>, PartAvailability> entry : ri.partToAvailabilityMap.entrySet()) {
			if (entry.getValue() == PartAvailability.available) {
				result.add( CompiledPartIdentification.from( compiledArtifactIdentification, entry.getKey().get()));
			}
		}
		return result;
	}
	
	
}
