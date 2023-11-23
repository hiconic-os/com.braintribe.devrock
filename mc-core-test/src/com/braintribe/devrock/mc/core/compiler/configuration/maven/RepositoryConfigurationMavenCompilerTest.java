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
package com.braintribe.devrock.mc.core.compiler.configuration.maven;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.compiler.AbstractCompilerTest;
import com.braintribe.devrock.mc.core.compiler.RepositoryConfigurationValidator;
import com.braintribe.devrock.mc.core.configuration.maven.ConceptualizedCloner;
import com.braintribe.devrock.mc.core.configuration.maven.MavenSettingsCompiler;
import com.braintribe.devrock.model.repository.ChecksumPolicy;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.marshaller.artifact.maven.settings.DeclaredMavenSettingsMarshaller;
import com.braintribe.model.artifact.maven.settings.Profile;
import com.braintribe.model.artifact.maven.settings.Property;
import com.braintribe.model.artifact.maven.settings.Settings;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class RepositoryConfigurationMavenCompilerTest extends AbstractCompilerTest {
	private DeclaredMavenSettingsMarshaller marshaller = new DeclaredMavenSettingsMarshaller();
	
	private static RepositoryConfiguration standardRepositoryConfiguration;
	private static RepositoryConfiguration simpleRepositoryConfiguration;
	private static RepositoryConfiguration disjunctPropertyResolvingConfiguration;
	private static RepositoryConfiguration updatePoliciesConfiguration;
	private static RepositoryConfiguration injectedConfiguration;
	private static RepositoryConfiguration simpleInjectedConfiguration;
	
	static {
		TimeSpan always = TimeSpan.T.create();
		always.setValue(0);		
		
		TimeSpan never = null;
		
		TimeSpan daily = TimeSpan.T.create();
		daily.setUnit( TimeUnit.day);
		daily.setValue( 1);
				
		
		standardRepositoryConfiguration = RepositoryConfiguration.T.create();
		standardRepositoryConfiguration.setLocalRepositoryPath( System.getProperty("user.home") + "/.m2/repository-groups/"); 
		
		MavenHttpRepository coreDev = MavenHttpRepository.T.create();
		coreDev.setName( "core-dev");
		coreDev.setUrl( "https://declared_url/core-dev/");
		coreDev.setUpdateTimeSpan(never);
		coreDev.setCheckSumPolicy( ChecksumPolicy.ignore);
		coreDev.setSnapshotRepo(false);		
		coreDev.setUser("devrock-tests-dummy");
		coreDev.setPassword("nonewhatsoever");
		
		standardRepositoryConfiguration.getRepositories().add(coreDev);
		
		MavenHttpRepository thirdParty = MavenHttpRepository.T.create();
		thirdParty.setName( "third-party");
		thirdParty.setUrl( "https://declared_url/third-party/");
		thirdParty.setUpdateTimeSpan(never);
		thirdParty.setCheckSumPolicy( ChecksumPolicy.ignore);
		thirdParty.setSnapshotRepo(false);	
		thirdParty.setUser("devrock-tests-dummy");
		thirdParty.setPassword("nonewhatsoever");
		
		standardRepositoryConfiguration.getRepositories().add(thirdParty);
		
		MavenHttpRepository devRock = MavenHttpRepository.T.create();
		devRock.setName( "devrock");
		devRock.setUrl( "https://declared_url/devrock/");
		devRock.setUpdateTimeSpan(never);
		devRock.setCheckSumPolicy( ChecksumPolicy.ignore);
		devRock.setSnapshotRepo(false);	
		devRock.setUser("devrock-tests-dummy");
		devRock.setPassword("nonewhatsoever");
		standardRepositoryConfiguration.getRepositories().add( devRock);
		
		// simple
		simpleRepositoryConfiguration = RepositoryConfiguration.T.create();
		simpleRepositoryConfiguration.setLocalRepositoryPath( System.getProperty("user.home") + "/.m2/repository-groups/");
		
		simpleRepositoryConfiguration.getRepositories().add( coreDev);
		simpleRepositoryConfiguration.getRepositories().add( thirdParty);
		
		// disjunct 
		disjunctPropertyResolvingConfiguration = RepositoryConfiguration.T.create();
		disjunctPropertyResolvingConfiguration.setLocalRepositoryPath( System.getProperty("user.home") + "/.m2/repository-groups/");
		
		disjunctPropertyResolvingConfiguration.getRepositories().add( coreDev);
		
		MavenHttpRepository coreDevAlternative = MavenHttpRepository.T.create();
		coreDevAlternative.setName( "core-dev-alternative");
		coreDevAlternative.setUrl( "https://declared_url/core-dev-alternative/");
		coreDevAlternative.setUpdateTimeSpan(never);
		coreDevAlternative.setCheckSumPolicy( ChecksumPolicy.ignore);
		coreDevAlternative.setSnapshotRepo(false);	
		coreDevAlternative.setUser("devrock-tests-dummy");
		coreDevAlternative.setPassword("nonewhatsoever");
		
		disjunctPropertyResolvingConfiguration.getRepositories().add( coreDevAlternative);
		
		
		// update policy 
		updatePoliciesConfiguration = RepositoryConfiguration.T.create();
		updatePoliciesConfiguration.setLocalRepositoryPath( System.getProperty("user.home") + "/.m2/repository-groups/");
		
		MavenHttpRepository uCoreDevRelease = MavenHttpRepository.T.create();
		uCoreDevRelease.setName( "core-dev");
		uCoreDevRelease.setUrl( "https://declared_url/third-party/");
		uCoreDevRelease.setCheckSumPolicy( ChecksumPolicy.ignore);
		uCoreDevRelease.setUpdateTimeSpan(never);		
		uCoreDevRelease.setUser("devrock-tests-dummy");
		uCoreDevRelease.setPassword("nonewhatsoever");
		
		updatePoliciesConfiguration.getRepositories().add(uCoreDevRelease);

		MavenHttpRepository uCoreDevSnapshot = MavenHttpRepository.T.create();
		uCoreDevSnapshot.setName( "core-dev");
		uCoreDevSnapshot.setUrl( "https://declared_url/third-party/");
		uCoreDevSnapshot.setSnapshotRepo(true);
		uCoreDevSnapshot.setCheckSumPolicy( ChecksumPolicy.ignore);
		uCoreDevSnapshot.setUpdateTimeSpan(always);		
		uCoreDevSnapshot.setUser("devrock-tests-dummy");
		uCoreDevSnapshot.setPassword("nonewhatsoever");

		updatePoliciesConfiguration.getRepositories().add(uCoreDevSnapshot);
		
		TimeSpan interval10 = TimeSpan.T.create();
		interval10.setUnit(TimeUnit.minute);
		interval10.setValue(10);		
		
		
		MavenHttpRepository uCoreDevAlternativeSnapshot = MavenHttpRepository.T.create();
		uCoreDevAlternativeSnapshot.setName( "core-dev-alternative");
		uCoreDevAlternativeSnapshot.setUrl( "https://declared_url/third-party/");
		
		
		uCoreDevAlternativeSnapshot.setUpdateTimeSpan( interval10);
		uCoreDevAlternativeSnapshot.setSnapshotRepo(true);
		uCoreDevAlternativeSnapshot.setUser("devrock-tests-dummy");
		uCoreDevAlternativeSnapshot.setCheckSumPolicy( ChecksumPolicy.ignore);
		uCoreDevAlternativeSnapshot.setPassword("nonewhatsoever");
		
		updatePoliciesConfiguration.getRepositories().add(uCoreDevAlternativeSnapshot);

		MavenHttpRepository uCoreDevAlternativeRelease = MavenHttpRepository.T.create();
		uCoreDevAlternativeRelease.setName( "core-dev-alternative");
		uCoreDevAlternativeRelease.setUrl( "https://declared_url/third-party/");
		uCoreDevAlternativeRelease.setUpdateTimeSpan( daily);
		uCoreDevAlternativeRelease.setCheckSumPolicy( ChecksumPolicy.ignore);
		uCoreDevAlternativeRelease.setSnapshotRepo(false);
		uCoreDevAlternativeRelease.setUser("devrock-tests-dummy");
		uCoreDevAlternativeRelease.setPassword("nonewhatsoever");
		
		updatePoliciesConfiguration.getRepositories().add(uCoreDevAlternativeRelease);
		
		MavenHttpRepository uCoreDevAlternative2Release = MavenHttpRepository.T.create();
		uCoreDevAlternative2Release.setName( "core-dev-alternative-two");
		uCoreDevAlternative2Release.setUrl( "https://declared_url/third-party/");		
		uCoreDevAlternative2Release.setUpdateTimeSpan(daily);		
		uCoreDevAlternative2Release.setCheckSumPolicy( ChecksumPolicy.ignore);
		uCoreDevAlternative2Release.setUser("devrock-tests-dummy");
		uCoreDevAlternative2Release.setPassword("nonewhatsoever");
		
		updatePoliciesConfiguration.getRepositories().add(uCoreDevAlternative2Release);
	
		
		injectedConfiguration = RepositoryConfiguration.T.create();
		injectedConfiguration.setLocalRepositoryPath("myDrive:/.m2/repository-groups/");
		injectedConfiguration.setOffline(true);
		
		MavenHttpRepository iDeclaredRelease = MavenHttpRepository.T.create();
		iDeclaredRelease.setName( "declared_repo");
		iDeclaredRelease.setUrl( "http://declared_url");
		iDeclaredRelease.setUpdateTimeSpan( null);
		iDeclaredRelease.setSnapshotRepo(false);		
		iDeclaredRelease.setUser("declared_user");
		iDeclaredRelease.setPassword("declared_password");
		iDeclaredRelease.setCheckSumPolicy( ChecksumPolicy.ignore);
		injectedConfiguration.getRepositories().add(iDeclaredRelease);
		
		MavenHttpRepository iDeclaredSnapshot = MavenHttpRepository.T.create();
		iDeclaredSnapshot.setName( "declared_repo");
		iDeclaredSnapshot.setUrl( "http://declared_url");
		iDeclaredSnapshot.setUser("declared_user");
		iDeclaredSnapshot.setPassword("declared_password");
		iDeclaredSnapshot.setUpdateTimeSpan( always);
		iDeclaredSnapshot.setSnapshotRepo(true);		
		iDeclaredSnapshot.setCheckSumPolicy( ChecksumPolicy.ignore);
		injectedConfiguration.getRepositories().add(iDeclaredSnapshot);
		
		MavenHttpRepository iDeclaredReleaseToOverride = MavenHttpRepository.T.create();
		iDeclaredReleaseToOverride.setName( "partially_declared_repo_split_to_override_all");
		iDeclaredReleaseToOverride.setUrl( "partially_declared_repo_split_to_override_all_url");
		iDeclaredReleaseToOverride.setUpdateTimeSpan( null);
		iDeclaredReleaseToOverride.setSnapshotRepo(false);		
		iDeclaredReleaseToOverride.setUser("partially_declared_repo_split_to_override_all_user");
		iDeclaredReleaseToOverride.setPassword("partially_declared_repo_split_to_override_all_password");
		iDeclaredReleaseToOverride.setCheckSumPolicy( ChecksumPolicy.ignore);
		injectedConfiguration.getRepositories().add(iDeclaredReleaseToOverride);
		
		MavenHttpRepository iDeclaredSnapshotToOverride = MavenHttpRepository.T.create();
		iDeclaredSnapshotToOverride.setName( "partially_declared_repo_split_to_override_all");
		iDeclaredSnapshotToOverride.setUrl( "partially_declared_repo_split_to_override_all_url");
		iDeclaredSnapshotToOverride.setUpdateTimeSpan( always);
		iDeclaredSnapshotToOverride.setSnapshotRepo(true);		
		iDeclaredSnapshotToOverride.setUser("partially_declared_repo_split_to_override_all_user");
		iDeclaredSnapshotToOverride.setPassword("partially_declared_repo_split_to_override_all_password");
		iDeclaredSnapshotToOverride.setCheckSumPolicy( ChecksumPolicy.ignore);
		injectedConfiguration.getRepositories().add(iDeclaredSnapshotToOverride);
		
		MavenHttpRepository iAdded = MavenHttpRepository.T.create();
		iAdded.setName("added_repo");
		iAdded.setUser("added_user");
		iAdded.setPassword("added_password");
		iAdded.setUrl("added_url");
		injectedConfiguration.getRepositories().add(iAdded);
		
		MavenHttpRepository iDeclaredReleaseToOverrideRelease = MavenHttpRepository.T.create();
		iDeclaredReleaseToOverrideRelease.setName( "partially_declared_repo_split_to_override_release");
		iDeclaredReleaseToOverrideRelease.setUrl( "partially_declared_repo_split_to_override_release_url");
		iDeclaredReleaseToOverrideRelease.setUpdateTimeSpan( daily);
		iDeclaredReleaseToOverrideRelease.setSnapshotRepo(false);		
		iDeclaredReleaseToOverrideRelease.setUser("partially_declared_repo_split_to_override_release_user");
		iDeclaredReleaseToOverrideRelease.setPassword("partially_declared_repo_split_to_override_release_password");
		iDeclaredReleaseToOverrideRelease.setCheckSumPolicy( ChecksumPolicy.ignore);
		injectedConfiguration.getRepositories().add(iDeclaredReleaseToOverrideRelease);
		
	}
		

	@Override
	protected String getRoot() {
		return "compiler/maven.settings";
	}

	@Before
	public void before() {		
		TestUtils.ensure( output);
	}
	
	Settings loadSettings( File file) {
		try (InputStream in = new FileInputStream( file)){			
			Settings settings = marshaller.unmarshall(in);		
			return settings;
		} catch (Exception e) {
			throw new IllegalStateException("cannot read file [" + file.getAbsolutePath() + "]", e);
		}
	}
	
	private void test(File file, RepositoryConfiguration expected) {
		Settings settings = loadSettings( file);
		MavenSettingsCompiler compiler = new MavenSettingsCompiler();
		compiler.setSettingsSupplier( () -> settings);
		RepositoryConfiguration repositoryConfiguration = compiler.get();
		if (expected != null) {
			RepositoryConfigurationValidator.validate( expected, repositoryConfiguration);
		
		}		
	}
	
	@Test
	@Category(KnownIssue.class)
	public void testStandard() {
		File file = new File( input, "settings.xml");
		test( file, standardRepositoryConfiguration);
	}
	
	
	@Test
	@Category(KnownIssue.class)
	public void testSimple() {
		File file = new File( input, "settings.simple.xml");
		test( file, simpleRepositoryConfiguration);
	}
	

	@Test
	@Category(KnownIssue.class)
	public void testDisjunct() {
		File file = new File( input, "settings.disjunct.profiles.xml");
		test( file, disjunctPropertyResolvingConfiguration);
	}
	@Test
	@Category(KnownIssue.class)
	public void testUpdatePolicies() {
		File file = new File( input, "settings.updatepolicy.xml");
		test( file, updatePoliciesConfiguration);
	}
	
	@Test
	@Category(KnownIssue.class)
	public void testYamlInjection() {
		File file = new File( input, "settings.yaml.injection.xml");
		test( file, injectedConfiguration);
	}
	
	@Test
	@Category(KnownIssue.class)
	public void testSimpleYamlInjection() {
		File file = new File( input, "simple.settings.yaml.injection.xml");
		test( file, simpleInjectedConfiguration);
	}
	
	@Test
	@Category(KnownIssue.class)
	public void testSimpleInitial() {
		File file = new File( input, "settings.simple.xml");
		Settings settings = loadSettings( file);
		
	
		Map<Profile, Map<String, String>> resolvedPropertiesMapOfProfile = new HashMap<>();
		for (Profile profile : settings.getProfiles()) {
			List<Property> properties = profile.getProperties();
			
			Map<String,String> effectivePropertiesMap = new HashMap<>();
			for (Property property : properties) {			
				effectivePropertiesMap.put( property.getName(), property.getValue());
			}
			resolvedPropertiesMapOfProfile.put( profile, effectivePropertiesMap);		
		}
		
		for (Profile profile : settings.getProfiles()) {
			ConceptualizedCloner cloner = new ConceptualizedCloner( );
			cloner.setEffectiveProperties(resolvedPropertiesMapOfProfile.get(profile));
			Profile resolvedProfile = profile.clone( ConfigurableCloningContext.build().withClonedValuePostProcesor( cloner::clonedValuePostProcessor).done());
			System.out.println(resolvedProfile);
		}		
	}
}

