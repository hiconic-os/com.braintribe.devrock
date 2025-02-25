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
package com.braintribe.artifacts.test.maven.settings.environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpertImpl;
import com.braintribe.model.maven.settings.Profile;



public class SettingsEnvironmentLab extends AbstractSettingsEnviromentLab {
	private static final String GLOBAL_SETTINGS = MavenSettingsPersistenceExpertImpl.ENV_GLOBAL_SETTINGS;
	private static final String LOCAL_SETTINGS = MavenSettingsPersistenceExpertImpl.ENV_LOCAL_SETTINGS;
	private static final String SETTINGS = MavenSettingsPersistenceExpertImpl.ENV_SETTINGS;
	
	private static File contents = new File( "res/settingsEnvironmentLab/contents");
	private static HashMap<String, File> settingsPairings = new HashMap<>();
	private static HashMap<String, List<String>> settingsProfiles = new HashMap<>();
	
	@BeforeClass
	public static void beforeClass() {
		settingsPairings.put( SETTINGS, new File( contents, "settings.single.xml"));
		settingsPairings.put( LOCAL_SETTINGS, new File( contents, "settings.user.xml"));
		settingsPairings.put( GLOBAL_SETTINGS, new File( contents, "settings.global.xml"));
		
		settingsProfiles.put( SETTINGS, Arrays.asList( "braintribe.SINGLE"));
		settingsProfiles.put( LOCAL_SETTINGS, Arrays.asList( "braintribe.USER"));
		settingsProfiles.put( GLOBAL_SETTINGS, Arrays.asList( "braintribe.GLOBAL"));
	}
	

	@Before 
	public void beforeTest() {		
		runBefore();
	}
	
	@Test
	public void testSingleFile() {	
		virtualEnvironment.addEnvironmentOverride( SETTINGS, settingsPairings.get( SETTINGS).getAbsolutePath());
		List<Profile> activeProfiles = mavenSettingsReader.getActiveProfiles();
		List<String> expectedProfileIds = settingsProfiles.get(SETTINGS);
		validate( activeProfiles, expectedProfileIds);
	}
	@Test
	public void testLocalFile() {
		virtualEnvironment.addEnvironmentOverride( SETTINGS, settingsPairings.get( LOCAL_SETTINGS).getAbsolutePath());
		List<Profile> activeProfiles = mavenSettingsReader.getActiveProfiles();
		List<String> expectedProfileIds = settingsProfiles.get(LOCAL_SETTINGS);
		validate( activeProfiles, expectedProfileIds);
	}
	@Test
	public void testGlobalFile() {
		virtualEnvironment.addEnvironmentOverride( SETTINGS, settingsPairings.get( GLOBAL_SETTINGS).getAbsolutePath());
		List<Profile> activeProfiles = mavenSettingsReader.getActiveProfiles();
		List<String> expectedProfileIds = settingsProfiles.get(GLOBAL_SETTINGS);
		validate( activeProfiles, expectedProfileIds);
	}
	@Test
	public void testMergedFiles() {
		virtualEnvironment.addEnvironmentOverride( SETTINGS, settingsPairings.get( LOCAL_SETTINGS).getAbsolutePath());
		virtualEnvironment.addEnvironmentOverride( SETTINGS, settingsPairings.get( GLOBAL_SETTINGS).getAbsolutePath());
		List<Profile> activeProfiles = mavenSettingsReader.getActiveProfiles();
		List<String> expectedGlobalProfileIds = settingsProfiles.get(GLOBAL_SETTINGS);		
		List<String> expectedLocalProfileIds = settingsProfiles.get( LOCAL_SETTINGS);
		
		List<String> combined = new ArrayList<>();
		combined.addAll(expectedLocalProfileIds);
		combined.addAll( expectedGlobalProfileIds);
		validate( activeProfiles, combined);
	}

	private void validate( List<Profile> profiles, List<String> expectedProfileIds) {
		List<String> unexpected = new ArrayList<>();
		List<String> retrieved = profiles.stream().map( p -> {return (String) p.getId();}).collect( Collectors.toList()); 
		for (Profile profile : profiles) {
			String profileId = profile.getId();
			if (!expectedProfileIds.contains(profileId)) {
				unexpected.add(profileId);
			}
			else {
				retrieved.remove( profileId);
			}
		}
		Assert.assertTrue( "unexpected, but delivered [" + unexpected.stream().collect( Collectors.joining(",")) + "]", unexpected.size() == 0);
		Assert.assertTrue( "expected, yet not delivered [" + retrieved.stream().collect( Collectors.joining(",")) + "]", retrieved.size() == 0);
	}
}
