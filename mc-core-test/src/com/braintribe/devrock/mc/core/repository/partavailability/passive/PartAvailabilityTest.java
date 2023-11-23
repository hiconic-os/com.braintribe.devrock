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
package com.braintribe.devrock.mc.core.repository.partavailability.passive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.repository.local.PartAvailability;
import com.braintribe.devrock.mc.api.repository.local.PartAvailabilityAccess;
import com.braintribe.devrock.mc.core.commons.ManagedFilesystemLockSupplier;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.filters.AllMatchingArtifactFilterExpert;
import com.braintribe.devrock.mc.core.repository.local.BasicArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.core.repository.local.LocalReleasePartAvailabilityAccess;
import com.braintribe.devrock.mc.core.repository.local.ReleasePartAvailabilityAccess;
import com.braintribe.devrock.mc.core.repository.local.SnapshotPartAvailabilityAccess;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.version.Version;
import com.braintribe.utils.FileTools;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;



/**
 * tests for the {@link PartAvailabilityAccess} and its implementations, {@link SnapshotPartAvailabilityAccess}, {@link ReleasePartAvailabilityAccess}, {@link LocalReleasePartAvailabilityAccess}
 * 
 * @author pit
 *
 */
public class PartAvailabilityTest implements HasCommonFilesystemNode {
	private File output; 
	private File repo;
	private File input;
	
	{
		Pair<File,File> pair = filesystemRoots("repository/pa");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");	
	}

	private static final String repoId = "mine";
	
	
	
	private Repository localRepository; 
	{
		localRepository = MavenHttpRepository.T.create();
		localRepository.setName("local");
	}
	
	private Repository remoteRepository;
	{
		remoteRepository = MavenHttpRepository.T.create();
		remoteRepository.setName( repoId);
	}
	
	
	private LoadingCache<EqProxy<ArtifactIdentification>, Set<EqProxy<Version>>> localVersionsCache;
	private DeclaredMavenMetaDataMarshaller metadataMarshaller = new DeclaredMavenMetaDataMarshaller();
	
	private  final List<Pair<PartIdentification, PartAvailability>> standardParts;
	private  final List<Pair<PartIdentification, PartAvailability>> unknownParts;
	private final  List<Pair<PartIdentification, PartAvailability>> unavailableParts;
	
	{
		standardParts = new ArrayList<>();	
		standardParts.add( Pair.of( PartIdentification.parse( ":jar"), PartAvailability.available));
		standardParts.add( Pair.of( PartIdentification.parse( ":pom"), PartAvailability.available));
		standardParts.add( Pair.of( PartIdentification.parse( "sources:jar"), PartAvailability.available));
		standardParts.add( Pair.of( PartIdentification.parse( "javadoc:jar"), PartAvailability.unavailable));
		
		unknownParts = new ArrayList<>();
		unknownParts.add( Pair.of( PartIdentification.parse( ":jar"), PartAvailability.unknown));
		unknownParts.add( Pair.of( PartIdentification.parse( ":pom"), PartAvailability.unknown));
		unknownParts.add( Pair.of( PartIdentification.parse( "sources:jar"), PartAvailability.unknown));
		unknownParts.add( Pair.of( PartIdentification.parse( "javadoc:jar"), PartAvailability.unknown));
		
		unavailableParts = new ArrayList<>();
		unavailableParts.add( Pair.of( PartIdentification.parse( ":jar"), PartAvailability.unavailable));
		unavailableParts.add( Pair.of( PartIdentification.parse( ":pom"), PartAvailability.unavailable));
		unavailableParts.add( Pair.of( PartIdentification.parse( "sources:jar"), PartAvailability.unavailable));
		unavailableParts.add( Pair.of( PartIdentification.parse( "javadoc:jar"), PartAvailability.unavailable));
		
		localVersionsCache = Caffeine.newBuilder().build( this::loadLocalMetaData);
	}
	
	@Before
	public void before() {
		TestUtils.ensure( repo);
	}
	
	private boolean doesLocalVersionExist( ArtifactIdentification artifactIdentification, Version version) {
		EqProxy<ArtifactIdentification> aiKey = HashComparators.artifactIdentification.eqProxy(artifactIdentification);
		EqProxy<Version> versionKey = HashComparators.version.eqProxy( version);
				
		return localVersionsCache.get(aiKey).contains(versionKey);		
	}

	
	/**
	 * creates an {@link PartAvailabilityAccess}
	 * @param artifact - the qualified artifact
	 * @param metadataFile - the {@link File} that contains the metadata 
	 * @return
	 */
	private PartAvailabilityAccess createPartAvailabilityAccess(String artifact, File metadataFile, Repository repository) {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(artifact);
		// depending on the qualifier, it's either for SNAPSHOT or RELEASE 
		String qualifier = cai.getVersion().getQualifier(); 		
		String repositoryId = repository.getName();
		FakeArtifactPartResolverPersistenceDelegate frd = new FakeArtifactPartResolverPersistenceDelegate( cai.getVersion(), null, metadataFile, null, null);
		ManagedFilesystemLockSupplier managedFilesystemLockSupplier = new ManagedFilesystemLockSupplier();
		if (qualifier != null && qualifier.equalsIgnoreCase("SNAPSHOT")) {
			if (repositoryId.equals( "local")) {
				return new SnapshotPartAvailabilityAccess(cai, managedFilesystemLockSupplier, AllMatchingArtifactFilterExpert.instance, repo, repository, BasicArtifactPartResolverPersistenceDelegate.createLocal());
			}
			else {
				return new SnapshotPartAvailabilityAccess(cai, managedFilesystemLockSupplier, AllMatchingArtifactFilterExpert.instance, repo, repository, frd);
			}
		}
		else {
			if (repositoryId.equals( "local")) {
				localVersionsCache.invalidateAll();
				return  new LocalReleasePartAvailabilityAccess(cai, managedFilesystemLockSupplier, AllMatchingArtifactFilterExpert.instance, repo, repository, BasicArtifactPartResolverPersistenceDelegate.createLocal(), this::doesLocalVersionExist);
			}
			else {
				return  new ReleasePartAvailabilityAccess(cai, managedFilesystemLockSupplier, AllMatchingArtifactFilterExpert.instance, repo, repository, frd);
			}
		}		
	}

	
	/**
	 * validate the {@link PartAvailabilityAccess}
	 * @param pa - the {@link PartAvailabilityAccess} to test
	 * @param pairs - the expectations : a {@link Collection} of {@link Pair} of {@link PartIdentification}, {@link PartAvailability} 
	 */
	private void validatePartExistence( PartAvailabilityAccess pa, Collection<Pair<PartIdentification, PartAvailability>> pairs) {
		for (Pair<PartIdentification, PartAvailability> pair : pairs) {
			PartAvailability availability = pa.getAvailability( pair.first);
			System.out.println( pair.first.asString() + " -> " + availability);		
			Assert.assertTrue("expected [" + pair.second + "] for [" + pair.first.asString() + "], found [" + availability + "]", availability == pair.second);
		}		
	}
	
	
	/**
	 * tests passive functionality of the snapshot part-availablity access  
	 */
	@Test
	public void testSnapshot() {
		PartAvailabilityAccess pa = createPartAvailabilityAccess( "com.braintribe.devrock.test:snapshot-artifact#1.0-SNAPSHOT", new File( input, "maven-metadata-snapshot.1.xml"), remoteRepository);
		Version version = pa.getActualVersion();
		System.out.println(version.asString());
		
		String expectedVersionAsString = "1.0-20161220.105848-901454234";		
		Assert.assertTrue( "expected [" + expectedVersionAsString + "], found [" + version.asString(), version.asString().equalsIgnoreCase( expectedVersionAsString));
		
		validatePartExistence(pa, standardParts);
					
	}
		

	/**
	 * tests passive functionality of the snapshot part-availablity access  
	 */
	@Test
	public void testLocalSnapshot() {
		String artifact = "com.braintribe.devrock.test:snapshot-artifact#1.0-SNAPSHOT";
		CompiledArtifactIdentification ci = CompiledArtifactIdentification.parse( artifact);
		File target = ArtifactAddressBuilder.build().root(repo.getAbsolutePath()).compiledArtifact(ci).metaData("local").toPath().toFile();
		File source = new File( input, "maven-metadata-snapshot.1.xml");
		FileTools.copyFile(source, target);
		
		// need to copy other files here ... 
		PartAvailabilityAccess pa = createPartAvailabilityAccess( "com.braintribe.devrock.test:snapshot-artifact#1.0-SNAPSHOT", new File( input, "maven-metadata-snapshot.1.xml"), localRepository);
		Version version = pa.getActualVersion();
		System.out.println(version.asString());
		
		String expectedVersionAsString = "1.0-20161220.105848-901454234";		
		Assert.assertTrue( "expected [" + expectedVersionAsString + "], found [" + version.asString(), version.asString().equalsIgnoreCase( expectedVersionAsString));
		
		validatePartExistence(pa, standardParts);
					
	}
	

		
	/**
	 * @param artifact - the qualified artifact to use
	 * @param pairs - the expectations : a {@link Collection} of {@link Pair} of {@link PartIdentification}, {@link PartAvailability}
	 */
	private PartAvailabilityAccess testRelease(String artifact, Collection<Pair<PartIdentification, PartAvailability>> pairs, Repository repository) {
		
		PartAvailabilityAccess pa = createPartAvailabilityAccess( artifact, null, repository);
		Version version = pa.getActualVersion();
		System.out.println(version.asString());
		
		String expectedVersionAsString = "1.0";		
		Assert.assertTrue( "expected [" + expectedVersionAsString + "], found [" + version.asString(), version.asString().equalsIgnoreCase( expectedVersionAsString));
		
		validatePartExistence(pa, pairs);
		
		return pa;
	}
	
	/**
	 * tests passive functionality of the release part-availability access   
	 */
	@Test
	public void testRelease() {
		
		// first run : no file, all's unknown
		String artifact = "com.braintribe.devrock.test:artifact#1.0";
		testRelease( artifact, unknownParts, remoteRepository);
		
		// after first run, copy the input file over 
		String file = "part-availability-mine.txt";
		CompiledArtifactIdentification ci = CompiledArtifactIdentification.parse( artifact);
		File target = ArtifactAddressBuilder.build().root(repo.getAbsolutePath()).compiledArtifact(ci).file( file).toPath().toFile();
		target.delete();
		File source = new File( input, file).getAbsoluteFile();		
		FileTools.copyFile(source, target);
		
		// second run : as defined in the file 
		testRelease( artifact, standardParts, remoteRepository);
	}
	
	/**
	 * tests active functionality of the release part-availability access  
	 */
	@Test
	public void testReleaseWrite() {
		// fi
		String artifact = "com.braintribe.devrock.test:artifact#1.0";
		PartAvailabilityAccess pa = testRelease( artifact, unknownParts, remoteRepository);
		
		for (Pair<PartIdentification, PartAvailability> pair : standardParts) {
			if (pair.second == PartAvailability.unknown)
				continue;
			pa.setAvailablity(pair.first, pair.second);
		}		
		testRelease( artifact, standardParts, remoteRepository);		
	}
	
	/**
	 * cheap copy of the loader functions for local maven metadata cache
	 * @param eqProxy
	 * @return
	 */
	private Set<EqProxy<Version>> loadLocalMetaData( EqProxy<ArtifactIdentification> eqProxy) {
		ArtifactIdentification ai = eqProxy.get();
		File file = ArtifactAddressBuilder.build().root( repo.getAbsolutePath()).artifact(ai).metaData("local").toPath().toFile();

		ReadWriteLock lock = new ManagedFilesystemLockSupplier().apply(file);
		Lock readLock = lock.readLock();
		readLock.lock();

		try {
			if (file.exists()) {
				Set<EqProxy<Version>> versions = new HashSet<>(); 
				try (InputStream in = new FileInputStream( file)){
					MavenMetaData md = (MavenMetaData) metadataMarshaller.unmarshall(in);
					Versioning versioning = md.getVersioning();
					if (versioning == null) {
						return Collections.emptySet();
					}
					for (Version version : versioning.getVersions()) {
						versions.add( HashComparators.version.eqProxy(version));
					}
					return versions;
				}
				catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
			else {
				return Collections.emptySet();
			}
		}
		finally {
			readLock.unlock();
		}
	}
	
	/**
	 * copies the named files to the artifact's directory 
	 * @param inputDirectory - the directory to find the sources
	 * @param artifact - a fully qualified artifact 
	 * @param names - the file names to copy 
	 */
	private void copy( File inputDirectory, String artifact, String ... names) {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(artifact);
		for (String name : names) {
			File source = new File( input, name);
			File target = ArtifactAddressBuilder.build().root(repo.getAbsolutePath()).compiledArtifact(cai).file( name).toPath().toFile();
			FileTools.copyFile(source, target);
		}
	}
	
	/**
	 * copies maven-metadata to the unversioned artifact's directory 
	 * @param inputDirectory - the directory to find the sources 
	 * @param artifact - the unversioned artifact
	 * @param name - the name of the source file
	 * @param repoId - the repository id 
	 */
	private void copyMetadata( File inputDirectory, String artifact, String name, String repoId) {
		File source = new File( input, name);
		ArtifactIdentification ai = ArtifactIdentification.parse(artifact);
		File target = ArtifactAddressBuilder.build().root(repo.getAbsolutePath()).artifact(ai).metaData( repoId).toPath().toFile();
		FileTools.copyFile(source, target);
	}
	
	
	/**
	 * tests passive functionality of the local release part-availability access
	 */
	@Test
	public void testLocalRelease() {
		// copy pom, jar, sources 
		String artifact = "com.braintribe.devrock.test:artifact#1.0";
		copy( input, artifact, "artifact-1.0.pom", "artifact-1.0.jar", "artifact-1.0-sources.jar");
		testRelease( artifact, unavailableParts, localRepository);
		
		// copy metadata 
		copyMetadata( input, "com.braintribe.devrock.test:artifact", "maven-metadata-local.xml", "local");
		testRelease( artifact, standardParts, localRepository);
		
	}
}
