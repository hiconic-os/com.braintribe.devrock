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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository.purge;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.ArtifactRemover;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.version.Version;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract base class for all 'install repository purge' tests 
 * @author pit
 *
 * a) test the correct removal of a single artifact which is the only artifact (no other versions)
 * b) test the correct removal of a single artifact which does have versioned siblings
 * c) test the correct removal of multiple artifacts in one go
 */

public abstract class AbstractRepositoryPurgingTest implements HasCommonFilesystemNode {

	protected static final String COMMON_CONTEXT_DEFINITION_YAML = "archive.definition.yaml";

	protected File repoCache;
	protected File repoInstall;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/repository/purge");
		input = pair.first;
		output = pair.second;
		repoCache = new File( output, "repo");
		repoInstall = new File( output, "install");
	}
	
	protected File config() { return new File( input, "repository-configuration.yaml");} 
	protected File initialCache = new File( input, "local-repo");
	protected File initialRepo = new File( input, "install-repo");
	
	protected TransitiveResolutionContext standardTransitiveResolutionContext = TransitiveResolutionContext.build().lenient( true).done();
	protected ClasspathResolutionContext standardClasspathResolutionContext = ClasspathResolutionContext.build().lenient(false).done();
	
	private Launcher launcher; 
	{
		launcher = Launcher.build()
				.repolet()
				.name("archive")
					.descriptiveContent()
						.descriptiveContent(archiveInput())
					.close()
				.close()							
			.done();
	}
	
	protected void additionalTasks() {}
	
	@Before
	public void runBefore() {
		
		
		// local repo -> cache
		TestUtils.ensure(repoCache);
		if (initialCache.exists()) {
			TestUtils.copy(initialCache, repoCache);
		}
		
		// install repo -> install 
		TestUtils.ensure(repoInstall);
		if (initialRepo.exists()) {
			TestUtils.copy(initialRepo, repoInstall);
		}	
		
		
		launcher.launch();		
		additionalTasks();
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	
	protected RepoletContent archiveInput() {
		return archiveInput(COMMON_CONTEXT_DEFINITION_YAML);
	};		
	
	protected RepoletContent archiveInput(String definition) {
		File file = new File( input, definition);
		try {
			return RepositoryGenerations.unmarshallConfigurationFile(file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot load parser file [" + file.getAbsolutePath() + "]" , IllegalStateException::new);
		} 
	}
		
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("cache", repoCache.getAbsolutePath());
		ove.setEnv("install", repoInstall.getAbsolutePath());
		ove.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, config().getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}

	
	
	protected RepositoryReflection getReflection() throws Exception {
		VirtualEnvironment ove = buildVirtualEnvironement(null);
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, new EnvironmentSensitiveConfigurationWireModule( ove)).build();
			) {			
			RepositoryReflection repositoryReflection = resolverContext.contract().dataResolverContract().repositoryReflection();			
			return repositoryReflection;																					
		}
	}
	
	protected void removeArtifact( VersionedArtifactIdentification vai, boolean single) {
	
		
		RepositoryConfiguration repositoryConfiguration = null;
		try {
			RepositoryReflection reflection = getReflection();
			repositoryConfiguration = reflection.getRepositoryConfiguration();
			
			 
		} catch (Exception e1) {
			e1.printStackTrace();
			Assert.fail("Resolution failed with an exception");
		}
		
		Validator validator = new Validator();
		Repository installRepository = repositoryConfiguration.getInstallRepository();
		
		validator.assertTrue("no install repository declared", installRepository != null);
		
		if (installRepository == null) {
			validator.assertResults();
			return;
		}
		
		validator.assertTrue("install repository is not a filesystem repo", installRepository instanceof MavenFileSystemRepository);
		
		if (installRepository instanceof MavenFileSystemRepository == false) {
			validator.assertResults();
			return;
		}
		
		MavenFileSystemRepository mfsr = (MavenFileSystemRepository) installRepository;
		
		String rp = mfsr.getRootPath();
		validator.assertTrue("install repository has no root path", rp != null);
		
		File rpFiler = new File( rp);
		

		Pair<Reason,File> pair = ArtifactRemover.removeArtifactFromFilesystemRepo(vai, rpFiler);
		
		if (pair.first != null) {		
			Reason reason = pair.first;
			validator.assertTrue( "reason returned : " + reason.stringify(), reason == null);
		}
		
		
		// find whether it's really gone
		
		// metadata 
		File unversionedArtifactDir = UniversalPath.from(rpFiler).pushDottedPath( vai.getGroupId()).push( vai.getArtifactId()).toFile();
		File mavenMetadataFile = new File( unversionedArtifactDir, "maven-metadata.xml");
		
		if (mavenMetadataFile.exists()) {
			validator.assertTrue( "unexpectedly, a maven-metdata.xml still exists", !single);
			
			if (!single) {
				MavenMetaData md = null;
				try (InputStream in = new FileInputStream(mavenMetadataFile)) {
					md = (MavenMetaData) DeclaredMavenMetaDataMarshaller.INSTANCE.unmarshall(in);																			
				}
				catch (Exception e) {	
					// can't unmarshall
					e.printStackTrace();
				}
				validator.assertTrue( "existing maven-metdata.xml cannot be read", md != null);
				if (md != null) {
					Version matchVersion = Version.parse( vai.getVersion());
					Versioning versioning = md.getVersioning();
					List<Version> versions = versioning.getVersions();
					Version matchedVersion = versions.stream().filter( v -> matchVersion.matches(v)).findFirst().orElse(null);					
					validator.assertTrue("unexpectedly the version still remains listed in maven-metadata.xml", matchedVersion == null);					
				}
				
			}
		}
		else {
			validator.assertTrue( "unexpectedly, a maven-metdata.xml doesn't exists", single);
		}
				
		File versionedArtifactDir = UniversalPath.from(unversionedArtifactDir).push( vai.getVersion()).toFile();
		
		validator.assertTrue("unexpectedly, the versioned artifact directory still exists", !versionedArtifactDir.exists());		
				
		validator.assertResults();			
	}
	
	protected void removeArtifacts( Map<VersionedArtifactIdentification, Boolean> map) {
	
		
		RepositoryConfiguration repositoryConfiguration = null;
		try {
			RepositoryReflection reflection = getReflection();
			repositoryConfiguration = reflection.getRepositoryConfiguration();
			
			 
		} catch (Exception e1) {
			e1.printStackTrace();
			Assert.fail("Resolution failed with an exception");
		}
		
		Validator validator = new Validator();
		Repository installRepository = repositoryConfiguration.getInstallRepository();
		
		validator.assertTrue("no install repository declared", installRepository != null);
		
		if (installRepository == null) {
			validator.assertResults();
			return;
		}
		
		validator.assertTrue("install repository is not a filesystem repo", installRepository instanceof MavenFileSystemRepository);
		
		if (installRepository instanceof MavenFileSystemRepository == false) {
			validator.assertResults();
			return;
		}
		
		MavenFileSystemRepository mfsr = (MavenFileSystemRepository) installRepository;
		
		String rp = mfsr.getRootPath();
		validator.assertTrue("install repository has no root path", rp != null);
		
		File rpFiler = new File( rp);
				
		List<VersionedArtifactIdentification> vais = new ArrayList<>( map.keySet());

		Pair<List<Reason>,List<File>> pair = ArtifactRemover.removeArtifactsFromFilesystemRepo(vais, rpFiler);
		
		
		
		if (pair.first != null) {			
			validator.assertTrue( "reasons returned", pair.first == null);
			
			for (Reason reason : pair.first) {
				System.out.println("Reason : " + reason.stringify());
			}
			
		}
			
		// find whether it's really gone
		for (Map.Entry<VersionedArtifactIdentification, Boolean> entry : map.entrySet()) {
			VersionedArtifactIdentification vai = entry.getKey();
			boolean single = entry.getValue();
						
			// metadata 
			File unversionedArtifactDir = UniversalPath.from(rpFiler).pushDottedPath( vai.getGroupId()).push( vai.getArtifactId()).toFile();
			File mavenMetadataFile = new File( unversionedArtifactDir, "maven-metadata.xml");
			
			if (mavenMetadataFile.exists()) {
				validator.assertTrue( "unexpectedly, a maven-metdata.xml still exists", !single);
				
				if (!single) {
					MavenMetaData md = null;
					try (InputStream in = new FileInputStream(mavenMetadataFile)) {
						md = (MavenMetaData) DeclaredMavenMetaDataMarshaller.INSTANCE.unmarshall(in);																			
					}
					catch (Exception e) {	
						// can't unmarshall
						e.printStackTrace();
					}
					validator.assertTrue( "existing maven-metdata.xml cannot be read", md != null);
					if (md != null) {
						Version matchVersion = Version.parse( vai.getVersion());
						Versioning versioning = md.getVersioning();
						List<Version> versions = versioning.getVersions();
						Version matchedVersion = versions.stream().filter( v -> matchVersion.matches(v)).findFirst().orElse(null);					
						validator.assertTrue("unexpectedly the version still remains listed in maven-metadata.xml", matchedVersion == null);					
					}
					
				}
			}
			else {
				validator.assertTrue( "unexpectedly, a maven-metdata.xml doesn't exists", single);
			}
					
			File versionedArtifactDir = UniversalPath.from(unversionedArtifactDir).push( vai.getVersion()).toFile();
			
			validator.assertTrue("unexpectedly, the versioned artifact directory still exists", !versionedArtifactDir.exists());
		
		}
				
		validator.assertResults();			
	}

	
}
