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
package com.braintribe.devrock.mc.core.wired.settings;


import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.compiler.RepositoryConfigurationValidator;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.configuration.maven.MavenSettingsLoader;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repository.ChecksumPolicy;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.RepositoryProbingMethod;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.logging.Logger;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
/**
 * These tests test the reading and interpretation of standard Maven settings.xml 
 * @author Dirk Scheffler
 *
 */
@Category(KnownIssue.class)
public class MavenSettingsCompilerTest implements LauncherTrait, HasCommonFilesystemNode {
	
	private static Logger log = Logger.getLogger(MavenSettingsCompilerTest.class);
		
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/compiling");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	private static final String MAVEN_ORG_URL = "https://repo1.maven.org/maven2";
	private static final String MAVEN_ORG_PROBING_PATH = "org/apache/maven/apache-maven/maven-metadata.xml";

	
	private RepositoryConfiguration overloadedSingleRepositoryConfiguration;
	private RepositoryConfiguration overloadedMultipleRepositoryConfiguration;
	
	private RepositoryConfiguration autodetectSingleRepositoryConfiguration;
	private RepositoryConfiguration autodetectMultipleRepositoryConfiguration;
	
	private RepositoryConfiguration autodetectMirroredRepositoryConfiguration;
	
	{		
		MavenHttpRepository archiveA = MavenHttpRepository.T.create();
		archiveA.setName("archiveA");
		archiveA.setUpdateTimeSpan(null);
		archiveA.setUrl("https://localhost:8080/archiveA/");
		archiveA.setCheckSumPolicy(ChecksumPolicy.fail);
		
		MavenHttpRepository archiveB = MavenHttpRepository.T.create();
		archiveB.setName("archiveB");
		archiveB.setUpdateTimeSpan(null);
		archiveB.setUrl("https://localhost:8080/archiveB/");
		archiveB.setCheckSumPolicy(ChecksumPolicy.fail);
		
		MavenHttpRepository archiveC = MavenHttpRepository.T.create();
		archiveC.setName("archiveC");
		archiveC.setUpdateTimeSpan(null);
		archiveC.setUrl("https://localhost:8080/archiveC/");
		archiveC.setCheckSumPolicy(ChecksumPolicy.fail);
		
		MavenHttpRepository archiveD = MavenHttpRepository.T.create();
		archiveD.setName("archiveD");
		archiveD.setUpdateTimeSpan(null);
		archiveD.setUrl("https://localhost:8080/archiveD/");
		archiveD.setCheckSumPolicy(ChecksumPolicy.fail);
		

		MavenHttpRepository centralRelease = MavenHttpRepository.T.create();
		centralRelease.setName( "central");
		centralRelease.setUrl( MAVEN_ORG_URL);
		centralRelease.setProbingMethod(RepositoryProbingMethod.get);
		centralRelease.setProbingPath( MAVEN_ORG_PROBING_PATH);
		centralRelease.setCheckSumPolicy(ChecksumPolicy.fail);
				
		MavenHttpRepository centralSnapshot = MavenHttpRepository.T.create();
		centralSnapshot.setName( "central");
		centralSnapshot.setSnapshotRepo(true);
		centralSnapshot.setUrl( MAVEN_ORG_URL);
		centralSnapshot.setProbingMethod(RepositoryProbingMethod.get);
		centralSnapshot.setProbingPath( MAVEN_ORG_PROBING_PATH);
		centralSnapshot.setCheckSumPolicy(ChecksumPolicy.fail);
		
		MavenHttpRepository mirroredCentralRelease = MavenHttpRepository.T.create();
		mirroredCentralRelease.setName( "central");
		mirroredCentralRelease.setUrl("https://localhost:8080/archiveA/");
		mirroredCentralRelease.setCheckSumPolicy(ChecksumPolicy.fail);
				
		MavenHttpRepository mirroredCentralSnapshot = MavenHttpRepository.T.create();
		mirroredCentralSnapshot.setName( "central");
		mirroredCentralSnapshot.setSnapshotRepo(true);
		mirroredCentralSnapshot.setUrl("https://localhost:8080/archiveA/");
		mirroredCentralSnapshot.setCheckSumPolicy(ChecksumPolicy.fail);
		
			
		overloadedSingleRepositoryConfiguration = RepositoryConfiguration.T.create();
		overloadedSingleRepositoryConfiguration.setLocalRepositoryPath(  repo.getAbsolutePath());
		overloadedSingleRepositoryConfiguration.getRepositories().add(archiveA);
		overloadedSingleRepositoryConfiguration.getRepositories().add(archiveB);
		
		overloadedMultipleRepositoryConfiguration = RepositoryConfiguration.T.create();
		overloadedMultipleRepositoryConfiguration.setLocalRepositoryPath(  "/user");
		overloadedMultipleRepositoryConfiguration.getRepositories().add(archiveA);
		overloadedMultipleRepositoryConfiguration.getRepositories().add(archiveB);
		overloadedMultipleRepositoryConfiguration.getRepositories().add(archiveC);
		overloadedMultipleRepositoryConfiguration.getRepositories().add(archiveD);

		
		autodetectSingleRepositoryConfiguration = RepositoryConfiguration.T.create();
		autodetectSingleRepositoryConfiguration.setLocalRepositoryPath( "/user");
		autodetectSingleRepositoryConfiguration.getRepositories().add(archiveA);
		autodetectSingleRepositoryConfiguration.getRepositories().add(archiveB);
		autodetectSingleRepositoryConfiguration.getRepositories().add( centralRelease);
		autodetectSingleRepositoryConfiguration.getRepositories().add( centralSnapshot);
		
		
		autodetectMultipleRepositoryConfiguration = RepositoryConfiguration.T.create();
		autodetectMultipleRepositoryConfiguration.setLocalRepositoryPath(  "/user");
		autodetectMultipleRepositoryConfiguration.getRepositories().add(archiveA);
		autodetectMultipleRepositoryConfiguration.getRepositories().add(archiveB);
		autodetectMultipleRepositoryConfiguration.getRepositories().add(archiveC);
		autodetectMultipleRepositoryConfiguration.getRepositories().add(archiveD);
		autodetectMultipleRepositoryConfiguration.getRepositories().add( centralRelease);
		autodetectMultipleRepositoryConfiguration.getRepositories().add( centralSnapshot);

		autodetectMirroredRepositoryConfiguration = RepositoryConfiguration.T.create();
		autodetectMirroredRepositoryConfiguration.setLocalRepositoryPath( "/user");
		autodetectMirroredRepositoryConfiguration.getRepositories().add(archiveA);
		autodetectMirroredRepositoryConfiguration.getRepositories().add(archiveB);
		autodetectMirroredRepositoryConfiguration.getRepositories().add( centralRelease);
		autodetectMirroredRepositoryConfiguration.getRepositories().add( centralSnapshot);
		
	}

	/**
	 * redirecting the settings compiler to an overriding single settings.xml 
	 */
	@Test
	public void singleOverloadTest() {
		String settings = UniversalPath.from(input).push( "remapped").push("single").push( "basic-settings.xml").toFilePath();		
		OverridingEnvironment ves = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ves.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, null);
		ves.setEnv(MavenSettingsLoader.ENV_EXCLUSIVE_SETTINGS,settings);
		ves.setEnv(MavenSettingsLoader.ENV_LOCAL_SETTINGS, null);
		ves.setEnv(MavenSettingsLoader.ENV_GLOBAL_SETTINGS, null);
		ves.setEnv("repo", repo.getAbsolutePath());	
		ves.setEnv( "port", "8080");
		
		try (
				WireContext<RepositoryConfigurationContract> context = Wire.contextBuilder( MavenConfigurationWireModule.INSTANCE).bindContract(VirtualEnvironmentContract.class, () -> ves).build();
		) {
			RepositoryConfiguration repositoryConfiguration = context.contract().repositoryConfiguration().get();
			
			// validate
			RepositoryConfigurationValidator.validate( overloadedSingleRepositoryConfiguration, repositoryConfiguration);
			
		}
		catch (Exception e) {
			log.error("single overload test failed", e);
			Assert.fail("exception [" + e.getMessage() + "] thrown");
		}			
	}
	
	/**
	 * redirecting the settings compiling to two overriding settings.xml, one for the user, one for the installation 
	 */
	@Test
	public void multipleOverloadTest() {
		String userSettings = UniversalPath.from(input).push( "remapped").push("multiple").push( "user-settings.xml").toFilePath();
		String installationSettings = UniversalPath.from(input).push( "remapped").push("multiple").push( "installation-settings.xml").toFilePath();
		OverridingEnvironment ves = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ves.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, null);
		ves.setEnv(MavenSettingsLoader.ENV_EXCLUSIVE_SETTINGS, null);
		ves.setEnv(MavenSettingsLoader.ENV_LOCAL_SETTINGS,userSettings);
		ves.setEnv(MavenSettingsLoader.ENV_GLOBAL_SETTINGS, installationSettings);
		ves.setEnv("repo", repo.getAbsolutePath());	
		ves.setEnv( "port", "8080");
		
		try (
				WireContext<RepositoryConfigurationContract> context = Wire.contextBuilder( MavenConfigurationWireModule.INSTANCE).bindContract(VirtualEnvironmentContract.class, () -> ves).build();
		) {
			RepositoryConfiguration repositoryConfiguration = context.contract().repositoryConfiguration().get();
			
			RepositoryConfigurationValidator.validate( overloadedMultipleRepositoryConfiguration, repositoryConfiguration);			
		}
		catch (Exception e) {
			log.error("multiple overload test failed", e);
			e.printStackTrace();
			Assert.fail("exception [" + e.getMessage() + "] thrown");
		}
	}
	
	
	/**
     * tests auto detection and 'central injection' on a single settings file 
	 */
	@Test
	public void singleAutoDetectTest() {		
		OverridingEnvironment ves = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ves.setEnv("repo", repo.getAbsolutePath());	
		ves.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, null);
		String m2_repo = UniversalPath.from(input).push( "autodetect").push("single").push( "user").toFilePath();
		ves.setProperty("user.home", m2_repo);
		ves.setEnv( "port", "8080");
		
		
		try (
				WireContext<RepositoryConfigurationContract> context = Wire.contextBuilder( MavenConfigurationWireModule.INSTANCE).bindContract(VirtualEnvironmentContract.class, () -> ves).build();
		) {
			RepositoryConfiguration repositoryConfiguration = context.contract().repositoryConfiguration().get();
			// validate
			RepositoryConfigurationValidator.validate( autodetectSingleRepositoryConfiguration, repositoryConfiguration);
			
		}
		catch (Exception e) {
			log.error("single autodetect test failed", e);
			Assert.fail("exception [" + e.getMessage() + "] thrown");
		}
	}
	
	/**
	 * tests auto detection and 'central injection' on a single settings file, as central's mirrored, 
	 * another url is used without overriding probing method/url
	 */
	@Test
	public void singleMirroredAutoDetectTest() {		
		OverridingEnvironment ves = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ves.setEnv("repo", repo.getAbsolutePath());
		ves.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, null);
		String m2_repo = UniversalPath.from(input).push( "autodetect").push("mirrored").push( "user").toFilePath();
		ves.setProperty("user.home", m2_repo);
		ves.setEnv( "port", "8080");
		
		
		try (
				WireContext<RepositoryConfigurationContract> context = Wire.contextBuilder( MavenConfigurationWireModule.INSTANCE).bindContract(VirtualEnvironmentContract.class, () -> ves).build();
		) {
			RepositoryConfiguration repositoryConfiguration = context.contract().repositoryConfiguration().get();
			// validate
			RepositoryConfigurationValidator.validate( autodetectMirroredRepositoryConfiguration, repositoryConfiguration);
			
		}
		catch (Exception e) {
			log.error("single autodetect test failed", e);
			Assert.fail("exception [" + e.getMessage() + "] thrown");
		}
	}
	
	/**
	 * tests auto detection and 'central injection' on multiple settings files
	 */
	@Test
	public void multipleAutoDetectTest() {		
		OverridingEnvironment ves = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ves.setEnv("repo", repo.getAbsolutePath());
		ves.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, null);
		
		String userHome = UniversalPath.from(input).push( "autodetect").push("multiple").push("user").toFilePath();		
		ves.setProperty("user.home", userHome);
		
		String m2_home = UniversalPath.from(input).push( "autodetect").push("multiple").push("home").toFilePath();
		ves.setEnv("M2_HOME", m2_home);
		ves.setEnv( "port", "8080");
				
		ves.setEnv("M2_REPO", repo.getAbsolutePath());
		
		try (
				WireContext<RepositoryConfigurationContract> context = Wire.contextBuilder( MavenConfigurationWireModule.INSTANCE).bindContract(VirtualEnvironmentContract.class, () -> ves).build();
		) {
			RepositoryConfiguration repositoryConfiguration = context.contract().repositoryConfiguration().get();
			// validate
			RepositoryConfigurationValidator.validate( autodetectMultipleRepositoryConfiguration, repositoryConfiguration);
			
		}
		catch (Exception e) {
			log.error("multiple autodetect test failed", e);
			e.printStackTrace();
			Assert.fail("exception [" + e.getMessage() + "] thrown");
		}
	}
	
	
}
