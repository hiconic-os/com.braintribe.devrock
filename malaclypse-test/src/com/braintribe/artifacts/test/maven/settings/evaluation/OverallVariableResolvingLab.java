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
package com.braintribe.artifacts.test.maven.settings.evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.model.maven.settings.Activation;
import com.braintribe.model.maven.settings.ActivationProperty;
import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Property;
import com.braintribe.model.maven.settings.Repository;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.maven.settings.Settings;


public class OverallVariableResolvingLab extends AbstractMavenSettingsLab{
	private static final String TARGET_REPOSITORY = "target-repository";
	private static final String TRUSTWORTHY_REPOSITORIES = "trustworthyRepositories";
	private static final String UPDATE_REFLECTING_REPOSITORIES = "updateReflectingRepositories";
	private static final String PROPERTY_VALUE = "propertyValue";
	private static final String PROPERTY_NAME = "propertyName";

	private static final String MIRROR_OF = "mirrorOf";

	private static final String URL = "url";
	private static final String PWD = "pwd";
	private static final String USER = "user";

	private static File contents = new File( "res/settingsVariablesLab/contents");

	private static File settings = new File( contents, "settings.xml");
	private static File properties = new File( contents, "variables.properties");
	
	private static File localRepository = new File ( contents, "repo");
	private static MavenSettingsReader reader;

	private static Map<String, Map<String, String>> dataForServers = new HashMap<>(); 
	private static Map<String, Map<String, String>> dataForMirrors = new HashMap<>();
	
	private static Map<String, Map<String, String>> dataForProperties = new HashMap<>();
	private static Map<String, Map<String, String>> dataForActivation = new HashMap<>();
	private static Map<String, Map<String, String>> dataForRepositories = new HashMap<>();
	

	@BeforeClass
	public static void before() {
		before(settings, localRepository);
		reader = getReader();
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
		Properties testProperties = new Properties();
		try (InputStream in = new FileInputStream( properties)) {
			testProperties.load(in);
		}
		catch (IOException e) {
			String msg = "cannot load the properties file [" + properties.getAbsolutePath() + "]";
			Assert.fail(msg);
			throw new IllegalStateException(msg, e);
		}
				
		for (String key : testProperties.stringPropertyNames()) {			
			String value = testProperties.getProperty(key);
			ove.addEnvironmentOverride(key, value);
		}
		
		reader.setVirtualEnvironment( ove);
		
		// setup test data 
		Map<String, String> dataForCentral = new HashMap<>();
		dataForCentral.put(USER, testProperties.getProperty("central.user"));//"CENTRAL_USER"
		dataForCentral.put(PWD, testProperties.getProperty("central.user.pwd"));//"CENTRAL_PWD"		
		dataForServers.put( "central", dataForCentral);
		
		Map<String, String> dataForCentralMirror = new HashMap<>();
		dataForCentralMirror.put(USER, testProperties.getProperty("central.mirror.user"));//"MIRROR_USER"
		dataForCentralMirror.put(PWD, testProperties.getProperty("central.mirror.user.pwd"));//"MIRROR_PWD"		
		dataForServers.put( "central.mirror", dataForCentralMirror);
		
		Map<String, String> dataForCoreDev = new HashMap<>();
		dataForCoreDev.put(USER, testProperties.getProperty("core-dev.user"));//"CORE-DEV_USER"
		dataForCoreDev.put(PWD, testProperties.getProperty("core-dev.user.pwd"));//"CORE-DEV_PWD"		
		dataForServers.put( "core-dev", dataForCoreDev);
		
		Map<String, String> dataForDevrock = new HashMap<>();
		dataForDevrock.put(USER, testProperties.getProperty("devrock.user"));//"DEVROCK_USER"
		dataForDevrock.put(PWD, testProperties.getProperty("devrock.user.pwd"));//"DEVROCK_PWD"		
		dataForServers.put( "devrock", dataForDevrock);
		
		
		Map<String, String> mirrorDataForCentral = new HashMap<>();
		mirrorDataForCentral.put( URL, testProperties.getProperty( "standard.mirror.url"));
		mirrorDataForCentral.put( MIRROR_OF, testProperties.getProperty( "standard.mirror.expression"));
		dataForMirrors.put( "central", mirrorDataForCentral);
		
		Map<String, String> mirrorDataForCentralMirror = new HashMap<>();
		mirrorDataForCentralMirror.put( URL, testProperties.getProperty( "central.mirror.url"));
		mirrorDataForCentralMirror.put( MIRROR_OF, testProperties.getProperty( "central.mirror.expression"));
		dataForMirrors.put( "central.mirror", mirrorDataForCentralMirror);
		
		
		Map<String, String> dataForThirdPartyRepo = new HashMap<>();
		dataForThirdPartyRepo.put( URL, testProperties.getProperty( "standard.mirror.url"));		
		dataForRepositories.put("third-party", dataForThirdPartyRepo);
		
		Map<String, String> dataForCoreDevRepo = new HashMap<>();
		dataForCoreDevRepo.put( URL, testProperties.getProperty("core-dev.repo.url"));		
		dataForRepositories.put("core-dev", dataForCoreDevRepo);
				
		Map<String, String> dataForDevrockRepo = new HashMap<>();
		dataForDevrockRepo.put (URL, testProperties.getProperty( "devrock.repo.url"));
		dataForRepositories.put("devrock", dataForDevrockRepo);
		
		Map<String, String> propertiesForCoreProfile = new HashMap<>();
		propertiesForCoreProfile.put( UPDATE_REFLECTING_REPOSITORIES, testProperties.getProperty( "trustworthy"));		
		propertiesForCoreProfile.put( "ravenhurst-url-third-party", testProperties.getProperty( "standard.mirror.ravenhurst"));
		propertiesForCoreProfile.put( "ravenhurst-url-core-dev", testProperties.getProperty( "core-dev.ravenhurst"));
		propertiesForCoreProfile.put( TRUSTWORTHY_REPOSITORIES, testProperties.getProperty( "trustworthy"));
		propertiesForCoreProfile.put( TARGET_REPOSITORY, testProperties.getProperty( "core-dev.target"));
		
		dataForProperties.put( "core", propertiesForCoreProfile);
		
		Map<String, String> propertiesForDevrockProfile = new HashMap<>();
		propertiesForDevrockProfile.put( UPDATE_REFLECTING_REPOSITORIES, testProperties.getProperty( "devrock.trustworthy"));		
		propertiesForDevrockProfile.put( "ravenhurst-url-devrock", testProperties.getProperty( "devrock.ravenhurst"));		
		propertiesForDevrockProfile.put( TRUSTWORTHY_REPOSITORIES, testProperties.getProperty( "devrock.trustworthy"));
		propertiesForDevrockProfile.put( TARGET_REPOSITORY, testProperties.getProperty( "devrock.trustworthy"));
		
		dataForProperties.put("devrock", propertiesForDevrockProfile);

		
		Map<String, String> dataForCoreActivation = new HashMap<>();
		dataForCoreActivation.put( PROPERTY_NAME, testProperties.getProperty("profile_switch"));
		dataForCoreActivation.put( PROPERTY_VALUE, testProperties.getProperty( "core-value"));		
		dataForActivation.put("core", dataForCoreActivation );
		
		Map<String, String> dataForDevrockActivation = new HashMap<>();
		dataForDevrockActivation.put( PROPERTY_NAME, testProperties.getProperty("profile_switch"));
		dataForDevrockActivation.put( PROPERTY_VALUE, testProperties.getProperty( "devrock-value"));				
		dataForActivation.put("devrock", dataForDevrockActivation );
		
	}

	
	@Test
	public void test() {
		Settings currentSettings = reader.getCurrentSettings();
		//
		// servers
		// 
		List<Server> servers = currentSettings.getServers();
		for (Server server : servers) {
			
			String username = server.getUsername();
			String password = server.getPassword();
			String id = server.getId();
			
			Map<String, String> dataForServer = dataForServers.get( id);
			if (dataForServer == null) {
				Assert.fail("unexpected server [" + id + "] found");
				continue;
			}
			if (!username.equalsIgnoreCase( dataForServer.get( USER))){
				Assert.fail("expected user name to be [" + dataForServer.get( USER) + "] yet found [" + username + "] for server ["+ id + "]");
			}
			if (!password.equalsIgnoreCase( dataForServer.get( PWD))){
				Assert.fail("expected password to be [" + dataForServer.get( PWD) + "] yet found [" + password + "] for server ["+ id + "]");
			}
			
		}
		//
		// mirrors
		//
		List<Mirror> mirrors = currentSettings.getMirrors();
		for (Mirror mirror : mirrors) {
			String url = mirror.getUrl();
			String mirrorOf = mirror.getMirrorOf();
			String id = mirror.getId();
			Map<String, String> dataForMirror = dataForMirrors.get(id);
			if (dataForMirror == null) {
				Assert.fail("unexpected mirror [" + id + "] found");
				continue;
			}
			if (!url.equalsIgnoreCase( dataForMirror.get( URL))) {
				Assert.fail("expected url to be [" + dataForMirror.get( URL) + "] yet found [" + url + "] for mirror ["+ id + "]");
			}
			if (!mirrorOf.equalsIgnoreCase( dataForMirror.get( MIRROR_OF))) {
				Assert.fail("expected mirror of expression to be [" + dataForMirror.get( MIRROR_OF) + "] yet found [" + mirrorOf + "] for mirror ["+ id + "]");
			}			
		}
		//
		// profiles
		// 
		List<Profile> profiles = currentSettings.getProfiles();
		for (Profile profile : profiles) {
			String profileId = profile.getId();
			// repos
			for (Repository repository : profile.getRepositories()) {
				String id = repository.getId();
				Map<String, String> repoData = dataForRepositories.get( id);
				if (repoData == null) {
					Assert.fail("unexpected repository[" + id + "] found");
					continue;
				}
				String url = repository.getUrl();
				if (!url.equalsIgnoreCase( repoData.get( URL))) {
					Assert.fail("expected url to be [" + repoData.get( URL) + "] yet found [" + url + "] for repository ["+ id + "]");					
				}
			}
			// activation
			Map<String, String> activationData = dataForActivation.get(profileId);
			if (activationData == null) {
				Assert.fail("unexpected profile [" + profileId + "] found");
				continue;
			}
			Activation activation = profile.getActivation();
			ActivationProperty activationProperty = activation.getProperty();
			String name = activationProperty.getName();
			if (!name.equalsIgnoreCase( activationData.get(PROPERTY_NAME))) {
				Assert.fail("expected activation property to be [" + activationData.get( PROPERTY_NAME) + "] yet found [" + name  + "] for profile ["+ profileId + "]");				
			}
			String value = activationProperty.getValue();
			if (!value.equalsIgnoreCase( activationData.get(PROPERTY_VALUE))) {
				Assert.fail("expected activation property to be [" + activationData.get( PROPERTY_VALUE) + "] yet found [" + value  + "] for profile ["+ profileId + "]");				
			}
			
			// properties
			Map<String, String> propertyData = dataForProperties.get( profileId);			
			for (Property property : profile.getProperties()) {
				String propertyName = property.getName();
				String propertyValue = property.getValue();
				String propertyRawValue = property.getRawValue();
				
				if (!propertyRawValue.contains( "${")) {
					continue;
				}
				if (propertyValue == null) {
					Assert.fail( "property [" + propertyName + "] of profile [" + profileId + "] hasn't a expanded value");
					continue;
				}
				if (!propertyValue.equalsIgnoreCase( propertyData.get( propertyName))) {
					Assert.fail( "expected value for property [" + propertyName + "] of profile [" + profileId + "] is [" + propertyData.get( propertyName) + "], but found [" + propertyValue + "]");
					continue;
				}
			}
		}
	}

}
