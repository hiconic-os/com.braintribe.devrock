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
package com.braintribe.marshaller.maven.settings.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.maven.settings.Activation;
import com.braintribe.model.maven.settings.ActivationProperty;
import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Property;
import com.braintribe.model.maven.settings.Repository;
import com.braintribe.model.maven.settings.RepositoryPolicy;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.maven.settings.Settings;

public class StandardSettingsTest extends AbstractSettingsTest implements Validator {

	private File testFile = new File( contents, "settings.xml");
	
	@Override
	public boolean validate(Settings settings) {
		// header 
		if (!validateHeader( settings, "${env.M2_REPO}/", false, false, false)) {
			return false;
		}
		
		//
		// servers
		//
		Server s_centralMirror = createServer("central.mirror", "devrock-tests-dummy", "nonewhatsoever", "664", "775", null, null);
		Server s_central = createServer("central", "devrock-tests-dummy", "nonewhatsoever", "664", "775", null, null);
		Server s_devrock = createServer("devrock", "devrock-tests-dummy", "nonewhatsoever", "664", "775", null, null);
		Server s_coreDev = createServer("core-dev", "devrock-tests-dummy", "nonewhatsoever", "664", "775", null, null);
		Server s_customAssets = createServer("custom-assets", "devrock-tests-dummy", "nonewhatsoever", "664", "775", null, null);
		
		List<Server> expectedServers = new ArrayList<>();
		expectedServers.add(s_centralMirror);
		expectedServers.add(s_central);
		expectedServers.add(s_devrock);
		expectedServers.add(s_coreDev);
		expectedServers.add(s_customAssets);
		
		if (!validateServers(settings.getServers(), expectedServers)) {
			return false;
		}
		
		//
		// mirrors
		// 
		Mirror m_centralMirror = createMirror("central.mirror", "DEVROCK_TESTS_REPOSITORY_BASE_URL/third-party/", "*,!devrock,!core-dev,!custom-assets");
		Mirror m_central = createMirror("central", "DEVROCK_TESTS_REPOSITORY_BASE_URL/third-party/", "*,!devrock,!core-dev,!custom-assets");
		Mirror m_devrock = createMirror("devrock", "DEVROCK_TESTS_REPOSITORY_BASE_URL/devrock/", "devrock");
		Mirror m_coreDev = createMirror("core-dev", "DEVROCK_TESTS_REPOSITORY_BASE_URL/core-dev/", "core-dev");
		Mirror m_customAssets = createMirror("custom-assets", "DEVROCK_TESTS_REPOSITORY_BASE_URL/custom-assets/", "custom-assets");
		
		List<Mirror> expectedMirrors = new ArrayList<>();
		expectedMirrors.add(m_centralMirror);
		expectedMirrors.add( m_central);
		expectedMirrors.add( m_devrock);
		expectedMirrors.add( m_coreDev);
		expectedMirrors.add( m_customAssets);
		
		if (!validateMirrors( settings.getMirrors(), expectedMirrors)) {
			return false;
		}
		
		//
		// profiles
		//
		RepositoryPolicy rp_releases = createRepositoryPolicy(true,"never", null);
		RepositoryPolicy rp_snapshots = createRepositoryPolicy(false, null, null);
		
		Property p_pa_updateReflectingRepositories = createProperty( "updateReflectingRepositories", "custom-assets");
		Property p_pa_ravenhurstUrlCustomAssets = createProperty("ravenhurst-url-custom-assets", "https://DEVROCK_TESTS_RAVENHURST_BASE_URL/custom-assets");
		Property p_pa_ravenhurstContextCustomAssets = createProperty("ravenhurst-context-custom-assets", "/");
		Property p_pa_trustworthyRepositories = createProperty( "trustworthyRepositories", "*");
	
		List<Property> p_pa_properties = new ArrayList<>();
		p_pa_properties.add(p_pa_updateReflectingRepositories);
		p_pa_properties.add(p_pa_ravenhurstContextCustomAssets);
		p_pa_properties.add( p_pa_ravenhurstUrlCustomAssets);
		p_pa_properties.add( p_pa_trustworthyRepositories);
		
		Repository r_platformAssets = createRepository("custom-assets", null, "DEVROCK_TESTS_REPOSITORY_BASE_URL/custom-assets/", "default", rp_releases, rp_snapshots);
		
		ActivationProperty p_ac_property = ActivationProperty.T.create();
		p_ac_property.setName( "env.PLATFORM_ASSETS");
		p_ac_property.setValue("ACTIVE");
		Activation p_activation = createActivation(false, null, p_ac_property, null, null);
		
		Profile pa_profile = createProfile("platform-assets", p_activation, p_pa_properties, Collections.singletonList(r_platformAssets), null);
		
		Repository r_thirdparty = createRepository("third-party", null, "DEVROCK_TESTS_REPOSITORY_BASE_URL/third-party/", "default", rp_releases, rp_snapshots);
		Repository r_devrock = createRepository("devrock", null, "DEVROCK_TESTS_REPOSITORY_BASE_URL/devrock/", "default", rp_releases, rp_snapshots);
		Repository r_coredev = createRepository("core-dev", null, "DEVROCK_TESTS_REPOSITORY_BASE_URL/core-dev/", "default", rp_releases, rp_snapshots);
	
		List<Repository> p_bt_repositories = new ArrayList<>();
		p_bt_repositories.add(r_thirdparty);
		p_bt_repositories.add( r_devrock);
		p_bt_repositories.add( r_coredev);
	
		Repository pr_thirdparty = createRepository("third-party", null, "DEVROCK_TESTS_REPOSITORY_BASE_URL/third-party/", "default", rp_releases, rp_snapshots);	
		Repository pr_coredev = createRepository("core-dev", null, "DEVROCK_TESTS_REPOSITORY_BASE_URL/core-dev/", "default", rp_releases, rp_snapshots);
	
		List<Repository> pr_bt_repositories = new ArrayList<>();
		pr_bt_repositories.add( pr_thirdparty);
		pr_bt_repositories.add(pr_coredev);
		
		
		Property p_bt_updateReflectingRepositories = createProperty( "updateReflectingRepositories", "third-party,devrock,core-dev,custom-assets");
		Property p_bt_trustworthyRepositories = createProperty( "trustworthyRepositories", "*");

		Property p_bt_ravenhurstContextTP = createProperty("ravenhurst-context-third-party", "/");
		Property p_bt_ravenhurstUrlTP = createProperty("ravenhurst-url-third-party", "https://DEVROCK_TESTS_RAVENHURST_BASE_URL/third-party");
		
		Property p_bt_ravenhurstContextDR = createProperty("ravenhurst-context-devrock", "/");
		Property p_bt_ravenhurstUrlDR = createProperty("ravenhurst-url-devrock", "https://DEVROCK_TESTS_RAVENHURST_BASE_URL/devrock");
	
		Property p_bt_ravenhurstContextCD = createProperty("ravenhurst-context-core-dev", "/");
		Property p_bt_ravenhurstUrlCD = createProperty("ravenhurst-url-core-dev", "https://DEVROCK_TESTS_RAVENHURST_BASE_URL/core-dev");
	
		List<Property> p_bt_properties = new ArrayList<>();
		p_bt_properties.add(p_bt_trustworthyRepositories);
		p_bt_properties.add( p_bt_updateReflectingRepositories);
		p_bt_properties.add( p_bt_ravenhurstContextTP);
		p_bt_properties.add( p_bt_ravenhurstUrlTP);
		p_bt_properties.add( p_bt_ravenhurstContextDR);
		p_bt_properties.add( p_bt_ravenhurstUrlDR);
		p_bt_properties.add( p_bt_ravenhurstContextCD);
		p_bt_properties.add( p_bt_ravenhurstUrlCD);
		
		Profile bt_profile = createProfile( "braintribe", null, p_bt_properties, p_bt_repositories, pr_bt_repositories);
		
		List<Profile> profiles = new ArrayList<>();
		profiles.add(pa_profile);
		profiles.add(bt_profile);
		
		if (!validateProfiles( settings.getProfiles(), profiles)) {
			return false;
		}
		
		List<Profile> activeProfiles = settings.getActiveProfiles();
		if (!validateProfiles(activeProfiles, profiles)) {
			return false;
		}
		
		return true;
	}

	@Test
	public void runStandardTest() {
		unmarshallAndValidate( testFile);
	}
	
	
}
