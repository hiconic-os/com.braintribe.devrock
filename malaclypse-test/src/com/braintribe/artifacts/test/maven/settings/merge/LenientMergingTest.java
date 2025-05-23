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
package com.braintribe.artifacts.test.maven.settings.merge;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.maven.settings.Activation;
import com.braintribe.model.maven.settings.ActivationFile;
import com.braintribe.model.maven.settings.ActivationOS;
import com.braintribe.model.maven.settings.ActivationProperty;
import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Property;
import com.braintribe.model.maven.settings.Proxy;
import com.braintribe.model.maven.settings.Repository;
import com.braintribe.model.maven.settings.RepositoryPolicy;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.maven.settings.Settings;

public  class LenientMergingTest extends AbstractMergingTest implements Validator {
	
	private File dominantFile = new File( mergeDir, "dominant.settings.xml");		
	private File emptyFile = new File( mergeDir, "empty.settings.xml");
	private File recessiveFile = new File( mergeDir, "recessive.settings.xml");
	
	@Test
	public void standardMergeTest() {
		Settings settings = testMerging( dominantFile, emptyFile);
		validateStandardMergeWithEmpty( settings);
	}

	private boolean validateStandardMergeWithEmpty(Settings settings) {
		// validate : all conflicts taken from first file aka dominant, others merged
				if (!validateHeader(settings, "dominant.repository", false, false, false)) {
					return false;
				}
				
				
				// servers
				List<Server> expectedServers = new ArrayList<>();
				Server centralServer = Server.T.create();
				centralServer.setId("central.mirror");
				centralServer.setUsername("dominant.user");
				centralServer.setPassword("dominant.pwd");
				centralServer.setFilePermissions( "664");
				centralServer.setDirectoryPermissions( "775");
				expectedServers.add(centralServer);				
				
				if (!validateServers(settings.getServers(), expectedServers)) {
					return false;
				}
				
				
				// mirrors
				List<Mirror> expectedMirrors = new ArrayList<>();
				Mirror centralMirror = Mirror.T.create();
				centralMirror.setId( "central.mirror");
				centralMirror.setUrl("http://archiva.bt.com/repository/standalone/");
				centralMirror.setMirrorOf("central");
				expectedMirrors.add(centralMirror);
				
				
				if (!validateMirrors(settings.getMirrors(), expectedMirrors)) {
					return false;
				}
				
				// plugin groups
				List<String> pluginGroups = settings.getPluginGroups();
				String [] expectedPluginGroups = new String [] {"org.mortbay.jetty"};
				
				if (!compareList( "plugin groups: ", pluginGroups, Arrays.asList( expectedPluginGroups))) {			
					return false;
				}
					
				// proxies
				List<Proxy> expectedProxies = new ArrayList<>();
				Proxy dominantProxy = createProxy( "myProxy", true, 8080, "proxy.somewhere.com", "http", "proxyuser", "somepassword", "*.braintribe.com,kwaqwagga.ch");
				expectedProxies.add(dominantProxy);				
				
				if (!validateProxies( settings.getProxies(), expectedProxies)) {
					return false;
				}
				// profile
				// 
				ActivationProperty dominantActivationProperty = ActivationProperty.T.create();
				dominantActivationProperty.setName("mavenVersion");
				dominantActivationProperty.setValue("2.0.3");
				
				ActivationOS dominantActivationOs = ActivationOS.T.create();
				dominantActivationOs.setFamily("Windows");
				dominantActivationOs.setArch("x86");
				dominantActivationOs.setName("Windows XP");
				dominantActivationOs.setVersion("5.1.2600");
				
				ActivationFile dominantActivationfile = ActivationFile.T.create();
				dominantActivationfile.setExists("${basedir}/file2.properties");
				dominantActivationfile.setMissing("${basedir}/file1.properties");
				
				Activation dominantActivation = createActivation(false, "1.5",dominantActivationProperty, dominantActivationOs, dominantActivationfile);
				
				List<Property> dominantProperties = new ArrayList<>();
				dominantProperties.add( createProperty("property_one", "dominant_value"));
				dominantProperties.add( createProperty("property_two", "dominant_value"));	
				
				RepositoryPolicy dominantReleases = createRepositoryPolicy(true, "never", "fail");
				RepositoryPolicy dominantSnapshots = createRepositoryPolicy(false, null, null);
				
				List<Repository> repositories = new ArrayList<>();
				repositories.add( createRepository("active", null, "http://localhost:8080/archiveA", "default", dominantReleases, dominantSnapshots));
								
				
				// 
				Profile dominantProfile = createProfile( "myProfile", dominantActivation, dominantProperties, repositories, null);							
				
				List<Profile> expectedProfiles = new ArrayList<>();
				expectedProfiles.add(dominantProfile);			
				if (!validateProfiles( settings.getProfiles(), expectedProfiles, Collections.singletonList("mc_origin"))) {
					return false;
				}
				return true;
		
	}

	@Test
	public void reversedMergeTest() {
		Settings settings = testMerging( emptyFile, recessiveFile);
		validateRecessiveMergeWithEmpty( settings);

	}

	private boolean validateRecessiveMergeWithEmpty(Settings settings) {
		// validate : all conflicts taken from first file aka dominant, others merged
		if (!validateHeader(settings, "recessive.repository", true, true, true)) {
			return false;
		}
		
		
		// servers
		List<Server> expectedServers = new ArrayList<>();
		Server centralServer = Server.T.create();
		centralServer.setId("central.mirror");
		centralServer.setUsername("recessive.user");
		centralServer.setPassword("recessive.pwd");
		centralServer.setFilePermissions( "664");
		centralServer.setDirectoryPermissions( "775");
		expectedServers.add(centralServer);
		
		Server recessiveServer = Server.T.create();
		recessiveServer.setId("recessive.mirror");
		recessiveServer.setUsername("recessive.user");
		recessiveServer.setPassword("recessive.pwd");
		recessiveServer.setFilePermissions( "664");
		recessiveServer.setDirectoryPermissions( "775");
		expectedServers.add( recessiveServer);
		
		if (!validateServers(settings.getServers(), expectedServers)) {
			return false;
		}
		
		
		// mirrors
		List<Mirror> expectedMirrors = new ArrayList<>();
		Mirror centralMirror = Mirror.T.create();
		centralMirror.setId( "central.mirror");
		centralMirror.setUrl("http://archiva.kwaqwagga.ch/repository/standalone/");
		centralMirror.setMirrorOf("central");
		expectedMirrors.add(centralMirror);
		
		Mirror recessiveMirror = Mirror.T.create();
		recessiveMirror.setId( "recessive.mirror");
		recessiveMirror.setUrl("http://archiva.kwaqwagga.ch/repository/standalone/");
		recessiveMirror.setMirrorOf("central");
		expectedMirrors.add(recessiveMirror);
		
		if (!validateMirrors(settings.getMirrors(), expectedMirrors)) {
			return false;
		}
		
		// plugin groups
		List<String> pluginGroups = settings.getPluginGroups();
		String [] expectedPluginGroups = new String [] {"org.mortbay.jetty", "recessive.org.mortbay.jetty"};
		
		if (!compareList( "plugin groups: ", pluginGroups, Arrays.asList( expectedPluginGroups))) {			
			return false;
		}
			
		// proxies
		List<Proxy> expectedProxies = new ArrayList<>();
		Proxy dominantProxy = createProxy( "myProxy", true, 8080, "recessive.proxy.somewhere.com", "http", "recessive.proxyuser", "recessive.somepassword", "*.google.com,ibiblio.org");
		expectedProxies.add(dominantProxy);
		Proxy recessiveProxy = createProxy( "recessive.myProxy", true, 8080, "recessive.proxy.somewhere.com", "http", "recessive.proxyuser", "recessive.somepassword", "*.recessive.com,*.recessive.ch");
		expectedProxies.add(recessiveProxy);
		
		if (!validateProxies( settings.getProxies(), expectedProxies)) {
			return false;
		}
		// profile
		// 
		ActivationProperty dominantActivationProperty = ActivationProperty.T.create();
		dominantActivationProperty.setName("mavenVersion");
		dominantActivationProperty.setValue("recessive.2.0.3");
		
		ActivationOS dominantActivationOs = ActivationOS.T.create();
		dominantActivationOs.setFamily("recessive.Windows");
		dominantActivationOs.setArch("recessive.x86");
		dominantActivationOs.setName("recessive.Windows XP");
		dominantActivationOs.setVersion("recessive.5.1.2600");
		
		ActivationFile dominantActivationfile = ActivationFile.T.create();
		dominantActivationfile.setExists("${basedir}/file2.recessive.properties");
		dominantActivationfile.setMissing("${basedir}/file1.recessive.properties");
		
		Activation dominantActivation = createActivation(true, "1.5",dominantActivationProperty, dominantActivationOs, dominantActivationfile);
		
		List<Property> dominantProperties = new ArrayList<>();
		dominantProperties.add( createProperty("property_one", "recessive_value"));
		dominantProperties.add( createProperty("property_two", "recessive_value"));	
		dominantProperties.add( createProperty("property_three", "recessive_value"));
		dominantProperties.add( createProperty("property_four", "recessive_value"));
		
		RepositoryPolicy dominantReleases = createRepositoryPolicy(false, null, null);
		RepositoryPolicy dominantSnapshots = createRepositoryPolicy(true, "always", "warn");
		
		List<Repository> repositories = new ArrayList<>();
		repositories.add( createRepository("active", null, "http://localhost:8080/archiveB", "default", dominantReleases, dominantSnapshots));
		
		
		RepositoryPolicy recessiveReleases = createRepositoryPolicy(false, "always", "warn");
		RepositoryPolicy recessiveSnapshots = createRepositoryPolicy(true, null, null);
		repositories.add( createRepository("recessive.active", null, "http://localhost:8080/archiveC", "default", recessiveReleases, recessiveSnapshots));
		
		
		// 
		Profile dominantProfile = createProfile( "myProfile", dominantActivation, dominantProperties, repositories, null);
		
		ActivationProperty recessiveActivationProperty = ActivationProperty.T.create();
		recessiveActivationProperty.setName("recessive.mavenVersion");
		recessiveActivationProperty.setValue("recessive.2.0.3");
		
		ActivationOS recessiveActivationOs = ActivationOS.T.create();
		recessiveActivationOs.setFamily("recessive.Windows");
		recessiveActivationOs.setArch("recessive.x86");
		recessiveActivationOs.setName("recessive.Windows XP");
		recessiveActivationOs.setVersion("recessive.5.1.2600");
		
		ActivationFile recessiveActivationfile = ActivationFile.T.create();
		recessiveActivationfile.setExists("${basedir}/file2.recessive.properties");
		recessiveActivationfile.setMissing("${basedir}/file1.recessive.properties");
		
		Activation recessiveActivation = createActivation(false, "1.5",recessiveActivationProperty, recessiveActivationOs, recessiveActivationfile);
		
		List<Property> recessiveProperties = new ArrayList<>();
		recessiveProperties.add( createProperty("recessive_property_one", "recessive_value"));
		recessiveProperties.add( createProperty("recessive_property_two", "recessive_value"));
	
		Profile recessiveProfile = createProfile("recessive.myProfile", recessiveActivation, recessiveProperties, null, null);
		
		List<Profile> expectedProfiles = new ArrayList<>();
		expectedProfiles.add(dominantProfile);
		expectedProfiles.add( recessiveProfile);
		if (!validateProfiles( settings.getProfiles(), expectedProfiles, Collections.singletonList("mc_origin"))) {
			return false;
		}
		return true;
		
	}

	@Test
	public void doubleMergeTest() {	
		try {
			Settings settings = testMerging( emptyFile, emptyFile);
			Assert.assertTrue("conjured some active profiles out of thin air", settings.getActiveProfiles().size() == 0);
			Assert.assertTrue("conjured some servers out of thin air", settings.getServers().size() == 0);
			Assert.assertTrue("conjured some mirrors out of thin air", settings.getMirrors().size() == 0);
			Assert.assertTrue("conjured some profiles out of thin air", settings.getProfiles().size() == 0);
		} catch (Exception e) {
			Assert.fail("execption [" + e.getMessage() + "] thrown" );
		}
		
	}
	

	
}
