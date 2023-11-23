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
package com.braintribe.artifacts.test.maven.settings.merge;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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



/**
 * tests merging: non conflicting values are merged, conflicting values are declared by the dominant file <br/>
 * in this setup, the following conflicts are introduced<br/>
 * <ul>
 * 	<li>local repository</li>
 * 	<li>server</li>
 * 	<li>profile</li>
 * </ul>
 * @author pit
 *
 */
public  class ReverseMergingTest extends AbstractMergingTest implements Validator {
	
	private File dominantFile = new File( mergeDir, "recessive.settings.xml");		
	private File recessiveFile = new File( mergeDir, "dominant.settings.xml");
	 
	
	@Override
	public boolean validate(Settings settings) {
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
	public void reverseMergeTest() {
		Settings settings = testMerging( dominantFile, recessiveFile);
		validate( settings);
		
	}

		
}
