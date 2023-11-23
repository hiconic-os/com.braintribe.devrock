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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;

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

public interface Validator {
	
	default boolean validateHeader(Settings settings, String localRepo, Boolean offline, Boolean interactiveMode, Boolean usePluginRegistry) {
		if (!compare("expected local repository [%s] yet found [%s]", settings.getLocalRepository(), localRepo)) {
			return false;
		}
		if (!compareBoolean( "expected offline flag [%s] yet found [%s]", settings.getOffline(), offline)) {
			Assert.fail();	
			return false;
		}
		if (!compareBoolean( "expected interactive mode flag [%s] yet found [%s]", settings.getInteractiveMode(), interactiveMode)) {
			return false;
		}
		
		if (!compareBoolean( "expected use plugin registry flag [%s] yet found [%s]", settings.getUsePluginRegistry(), usePluginRegistry)) {
			return false;
		}
		return true;
	}
	
	default boolean validateServers( List<Server> foundServers, Collection<Server> expectedServers)  {
		if (foundServers.size() != expectedServers.size()) {
			Assert.fail("expected number of servers [" + expectedServers.size() + "], yet found " + (foundServers == null ? "0"  : foundServers.size()) + "]");
			return false;
		}
		List<Server> testServers = new ArrayList<>( expectedServers);
		Iterator<Server> iterator = testServers.iterator();
		while (iterator.hasNext()) {
			Server server = iterator.next();
			for (Server foundServer : foundServers) {
				if (foundServer.getId().equals( server.getId())) {
					if (!validateServer( foundServer, server)) {
						return false;
					}
					iterator.remove();
				}
			}
		}
		if (testServers.size() != 0) {
			Assert.fail("not all expected servers found");
			return false;
		}
		return true;
	}
	
	default boolean validateServer( Server found, String id, String user, String pwd, String filePermissions, String directoryPermissions, String passphrase, String privateKey) {
		Server expected = createServer(id, user, pwd, filePermissions, directoryPermissions, passphrase, privateKey);
		
		return validateServer( found, expected);
	}

	default Server createServer(String id, String user, String pwd, String filePermissions, String directoryPermissions, String passphrase, String privateKey) {
		Server expected = Server.T.create();
		expected.setId(id);
		expected.setUsername(user);
		expected.setPassword( pwd);
		expected.setFilePermissions(filePermissions);
		expected.setDirectoryPermissions(directoryPermissions);
		expected.setPassphrase(passphrase);
		expected.setPrivateKey(privateKey);
		return expected;
	}
	
	default boolean compare( String msg, String found, String expected) {
		if (found == null && expected == null)
			return true;
		if (expected == null) {
			Assert.fail( String.format( msg,  expected, found));
			return false;
		}
		if (!expected.equalsIgnoreCase( found)) {
			Assert.fail( String.format( msg,  expected, found));
			return false;
		}
		return true;
	}
	default boolean compareBoolean( String msg, Boolean found, Boolean expected) {
		if (found == null && expected == null)
			return true;
		
		if (expected == null) {
			Assert.fail( String.format( msg,  expected, found));
			return false;
		}

		if (expected == false && found == null)
			return true;
		if (!expected.equals( found)) {
			Assert.fail( String.format( msg,  expected, found));
			return false;
		}
		return true;
	}
	
	default boolean compareInteger( String msg, Integer found, Integer expected) {
		if (found == null && expected == null)
			return true;
		
		if (expected == null) {
			Assert.fail( String.format( msg,  expected, found));
			return false;
		}		
		if (!expected.equals( found)) {
			Assert.fail( String.format( msg,  expected, found));
			return false;
		}
		return true;
	}
	
	default boolean compareList(String context,  List<String> found, List<String> expected) {
		if (found.size() != expected.size()) {
			Assert.fail( context + " expected number of entries [" + expected.size() + "], yet found [" + (found == null ? "0"  : found.size()) + "]");
			return false;
		}
		List<String> values = new ArrayList<>( expected);
		Iterator<String> iterator = values.iterator();
		while (iterator.hasNext()) {
			String value = iterator.next();
			if (found.contains(value)) 
				iterator.remove();
		}
		if (values.size() != 0) {
			for (String value: values) {
				System.out.println( "missing : " + value);
			}
			Assert.fail( context + " not all expected values found");
			return false;
		}
		return true;
	}
	
	
	default boolean validateServer( Server found, Server expected) {
		String context = "Server";
		if (!compare ( context + " expected id [%s], but found [%s]", found.getId(), expected.getId())) {
			return false;
		}
		if (!compare ( context + " expected user [%s], but found [%s]", found.getUsername(), expected.getUsername())) {
			return false;
		}
		if (!compare ( context + " expected pwd [%s], but found [%s]", found.getPassword(), expected.getPassword())) {
			return false;
		}
		if (!compare ( context + " expected file permission [%s], but found [%s]", found.getFilePermissions(), expected.getFilePermissions())) {
			return false;
		}
		if (!compare ( context + " expected directory permission [%s], but found [%s]", found.getDirectoryPermissions(), expected.getDirectoryPermissions())) {
			return false;
		}
		if (expected.getPassphrase() != null) {
			if (!compare ( context + " expected passphrase [%s], but found [%s]", found.getPassphrase(), expected.getPassphrase())) {
				return false;
			}
		}
		if (expected.getPrivateKey() != null) {
			if (!compare ( context + " expected private key [%s], but found [%s]", found.getPrivateKey(), expected.getPrivateKey())) {
				return false;
			}
		}
		return true;
	}
	default boolean validateMirror( Mirror found, String id, String url, String mirrorOf) {
		Mirror expected = createMirror(id, url, mirrorOf);
		return validateMirror( found, expected);
	}

	default Mirror createMirror(String id, String url, String mirrorOf) {
		Mirror expected = Mirror.T.create();
		expected.setId(id);
		expected.setUrl(url);
		expected.setMirrorOf(mirrorOf);
		return expected;
	}
	
	default boolean validateMirror( Mirror found, Mirror expected) {
		String context = "Mirror";
		if (!compare ( context + " expected id [%s], but found [%s]", found.getId(), expected.getId() )) {
			return false;
		}
		if (!compare ( context + " expected url [%s], but found [%s]", found.getUrl(), expected.getUrl())) {
			return false;
		}
		if (!compare ( context + " expected mirrorof [%s], but found [%s]", found.getMirrorOf(), expected.getMirrorOf())) {
			return false;
		}
		return true;
	}
	
	
	default boolean validateMirrors( List<Mirror> foundMirrors, Collection<Mirror> expectedMirrors)  {
		if (foundMirrors.size() != expectedMirrors.size()) {
			Assert.fail("expected number of mirrors [" + expectedMirrors.size() + "], yet found " + foundMirrors == null ? "0"  : foundMirrors.size() + "]");
			return false;
		}
		List<Mirror> testMirrors = new ArrayList<>( expectedMirrors);
		Iterator<Mirror> iterator = testMirrors.iterator();
		while (iterator.hasNext()) {
			Mirror mirror = iterator.next();
			for (Mirror foundMirror : foundMirrors) {
				if (foundMirror.getId().equals( mirror.getId())) {
					if (!validateMirror( foundMirror, mirror)) {
						return false;
					}
					iterator.remove();
				}
			}
		}
		if (testMirrors.size() != 0) {
			for (Mirror mirror : testMirrors) {
				System.out.println( "missing : " + mirror.getId());
			}
			Assert.fail("not all expected mirrors found");
			return false;
		}
		
		return true;
	}
	
	
	default Proxy createProxy(String id, boolean active, int port, String host, String protocol, String username, String password, String nonProxyHosts) {
		Proxy proxy = Proxy.T.create();
		proxy.setId(id);
		proxy.setActive( active);
		proxy.setPort( port);
		proxy.setHost(host);
		proxy.setProtocol(protocol);
		proxy.setUsername(username);
		proxy.setPassword(password);
		proxy.setNonProxyHosts(nonProxyHosts);
		return proxy;
	}
	
	default boolean validateProxy( Proxy found, Proxy expected) {
		String context = "Proxy";
		if (!compareBoolean( context + " active expected to be [%s], but found [%s]", found.getActive(), expected.getActive())){
			return false;
		}
		if (!compareInteger( context + " port expected to be [%d], but found [%d]", found.getPort(), expected.getPort())){
			return false;
		}
		if (!compare( context + " host expected to be [%s], but found [%s]", found.getHost(), expected.getHost())){
			return false;
		}
		if (!compare( context + " protocol expected to be [%s], but found [%s]", found.getProtocol(), expected.getProtocol())){
			return false;
		}
		if (!compare( context + " username expected to be [%s], but found [%s]", found.getUsername(), expected.getUsername())){
			return false;
		}
		if (!compare( context + " password expected to be [%s], but found [%s]", found.getPassword(), expected.getPassword())){
			return false;
		}
		if (!compare( context + " non proxy hosts expected to be [%s], but found [%s]", found.getNonProxyHosts(), expected.getNonProxyHosts())){
			return false;
		}
		
		return true;
	}
	
	default boolean validateProxies( Collection<Proxy> foundProxies, Collection<Proxy> expectedProxies) {
		if (foundProxies.size() != expectedProxies.size()) {
			Assert.fail("expected number of proxies [" + expectedProxies.size() + "], yet found " + (foundProxies == null ? "0"  : foundProxies.size()) + "]");
			return false;
		}
		List<Proxy> testProxies = new ArrayList<>( expectedProxies);
		Iterator<Proxy> iterator = testProxies.iterator();
		while (iterator.hasNext()) {
			Proxy proxy = iterator.next();
			for (Proxy foundProxy : foundProxies) {
				if (foundProxy.getId().equals( proxy.getId())) {
					if (!validateProxy( foundProxy, proxy)) {
						return false;
					}
					iterator.remove();
				}
			}
		}
		if (testProxies.size() != 0) {
			for (Proxy proxy : testProxies) {
				System.out.println( "missing : " + proxy.getId());
			}
			Assert.fail("not all expected proxies found");
			return false;
		}
		
		return true;
	}

	
	
	default boolean validateRepository( Repository found, Repository expected) {
		String context = "Repository";
		if (!compare ( context + " expected id [%s], but found [%s]", found.getId(), expected.getId())) {
			return false;
		}
		if (!compare ( context + " expected url [%s], but found [%s]", found.getUrl(), expected.getUrl())) {
			return false;
		}
		if (!compare ( context + " expected layout [%s], but found [%s]", found.getLayout(), expected.getLayout())) {
			return false;
		}
		if (!compare ( context + " expected name [%s], but found [%s]", found.getName(), expected.getName())) {
			return false;
		}
		
		RepositoryPolicy foundPolicy = found.getReleases();
		RepositoryPolicy expectedPolicy = expected.getReleases();
		
		if (!compare ( context + " expected releases update policy  [%s], but found [%s]", foundPolicy.getUpdatePolicy(), expectedPolicy.getUpdatePolicy())) {
			return false;
		}
		if (!compare ( context + " expected releases checksum policy  [%s], but found [%s]", foundPolicy.getChecksumPolicy(), expectedPolicy.getChecksumPolicy())) {
			return false;
		}
		if (!compareBoolean ( context + " expected releases checksum policy  [%s], but found [%s]", foundPolicy.getEnabled(), expectedPolicy.getEnabled())) {
			return false;
		}
		
		foundPolicy = found.getSnapshots();
		expectedPolicy = expected.getSnapshots();
		

		if (!compare ( context + " expected snapshots update policy  [%s], but found [%s]", foundPolicy.getUpdatePolicy(), expectedPolicy.getUpdatePolicy())) {
			return false;
		}
		if (!compare ( context + " expected snapshots checksum policy  [%s], but found [%s]", foundPolicy.getChecksumPolicy(), expectedPolicy.getChecksumPolicy())) {
			return false;
		}
		if (!compareBoolean ( context + " snapshots releases checksum policy  [%s], but found [%s]", foundPolicy.getEnabled(), expectedPolicy.getEnabled())) {
			return false;
		}
		
		return true;
	}
	
	default RepositoryPolicy createRepositoryPolicy( boolean enabled, String updatePolicy, String checkSumPolicy) {
		RepositoryPolicy policy = RepositoryPolicy.T.create();
		policy.setEnabled( enabled);
		policy.setUpdatePolicy(updatePolicy);
		policy.setChecksumPolicy(checkSumPolicy);
		return policy;
	}
	
	default Repository createRepository( String id, String name, String url, String layout, RepositoryPolicy releases, RepositoryPolicy snapshots) {
		Repository repository = Repository.T.create();
		repository.setId(id);
		repository.setName(name);
		repository.setLayout(layout);
		repository.setUrl(url);
		repository.setReleases(releases);
		repository.setSnapshots(snapshots);
		return repository;
	}
	
	default boolean validateRepositories( List<Repository> repositories, Collection<Repository> expectedRepositories) {
		if (repositories.size() != expectedRepositories.size()) {
			Assert.fail("expected [" + expectedRepositories.size() + "] repositories, but found [" + repositories.size() + "]");
			return false;
		}
		List<Repository> testRepositories = new ArrayList<>( expectedRepositories);
		Iterator<Repository> iterator = testRepositories.iterator();
		while (iterator.hasNext()) {
			Repository repository = iterator.next();
			for (Repository foundRepository : repositories) {
				if (foundRepository.getId().equals( repository.getId())) {
					if (!validateRepository( foundRepository, repository)) {
						return false;
					}
					iterator.remove();
				}
			}
		}
		if (testRepositories.size() != 0) {
			Assert.fail("not all expected repositories found");
			for (Repository repository : testRepositories) {
				System.out.println("missing : " + repository.getId());
			}
			return false;
		}
		return true;
	}
	
	default Activation createActivation( boolean byDefault, String jdk, ActivationProperty property, ActivationOS os, ActivationFile file) {
		Activation activation = Activation.T.create();
		activation.setActiveByDefault( byDefault);
		activation.setJdk(jdk);
		activation.setProperty( property);
		activation.setOs(os);
		activation.setFile(file);
		return activation;
	}
	
	default boolean validateActivation( Activation found, Activation expected) {
		if (found == null && expected == null)
			return true;
		String context = "activation";
		if (!compareBoolean( context + " activation expected to be [%s], but found [%s]", found.getActiveByDefault(), expected.getActiveByDefault())) {
			return false;
		}
		if (!compare( context + " jdk excepted to be [%s], but found [%s]", found.getJdk(), expected.getJdk())) {
			return false;
		}
		ActivationProperty foundProperty = found.getProperty();
		ActivationProperty expectedProperty = expected.getProperty();
		
		if (expectedProperty != null) {
			if (foundProperty != null) {
				if (!compare( context + " property's name expected to be [%s], but found [%s]", foundProperty.getName(), expectedProperty.getName())) {
					return false;
				}
				if (!compare( context + " property's value expected to be [%s], but found [%s]", foundProperty.getValue(), expectedProperty.getValue())) {
					return false;
				}				
			}
			else {
				Assert.fail("no matching activation property found");
				return false;
			}
		}
		else {
			if (foundProperty != null) {
				Assert.fail("unexpected activation property found");
				return false;
			}
		}
		
		
		ActivationFile foundFile = found.getFile();
		ActivationFile expectedFile = expected.getFile();
		if (expectedFile != null) {
			if (foundFile != null) {
				if (!compare( context + " file's existing tag expected to be [%s], but found [%s]", foundFile.getExists(), expectedFile.getExists())) {
					return false;
				}
				
				if (!compare( context + " file's missing tag expected to be [%s], but found [%s]", foundFile.getMissing(), expectedFile.getMissing())) {
					return false;
				}				
			}
			else {
				Assert.fail("no matching activation file found");
				return false;
			}
		}
		else {
			if (foundFile != null) {
				Assert.fail("unexpected activation file found");
				return false;
			}
		}
		
		ActivationOS foundOs = found.getOs();
		ActivationOS expectedOs = expected.getOs();
		
		if (expectedOs != null) {
			if (foundOs != null) {
				if (!compare( context + " OS's family tag expected to be [%s], but found [%s]", foundOs.getFamily(), expectedOs.getFamily())) {
					return false;
				}
				if (!compare( context + " OS's name tag expected to be [%s], but found [%s]", foundOs.getName(), expectedOs.getName())) {
					return false;
				}
				if (!compare( context + " OS's arch tag expected to be [%s], but found [%s]", foundOs.getArch(), expectedOs.getArch())) {
					return false;
				}
				if (!compare( context + " OS's version tag expected to be [%s], but found [%s]", foundOs.getVersion(), expectedOs.getVersion())) {
					return false;
				}				
			}
			else  {
				Assert.fail("no matching activation os found");
				return false;
			}
		}
		else if (foundOs != null) {
			Assert.fail("unexpected activation os found");
			return false;
		}
		
		
		return true;
	}
	
	default Property createProperty( String name, String value) {
		Property property = Property.T.create();
		property.setName(name);
		property.setRawValue(value);
		return property;
	}
	
	default boolean validateProperty( Property found, Property expected) {
		String context = "Property [" + found.getName() + "]";
		if (!compare( context + " name tag expected to be [%s], but found [%s]", found.getName(), expected.getName())) {
			return false;
		}
		if (!compare( context + " value tag expected to be [%s], but found [%s]", found.getRawValue(), expected.getRawValue())) {
			return false;
		}
		return true;
	}
	
	default boolean validateProperties( Profile profile, Collection<Property> expectedProperties, List<String> propertiesToIgnore) {
		List<Property> properties = profile.getProperties().stream().filter( p -> !propertiesToIgnore.contains( p.getName())).collect(Collectors.toList());
	
		if (properties.size() != expectedProperties.size()) {
			Assert.fail("expected [" + expectedProperties.size() + "] properties, but found [" + properties.size() + "]");
			return false;
		}
		List<Property> testProperties = new ArrayList<>( expectedProperties);
		Iterator<Property> iterator = testProperties.iterator();
		while (iterator.hasNext()) {
			Property property = iterator.next();
			for (Property foundProperty : properties) {
				if (property.getName().equals( foundProperty.getName())) {
					if (!validateProperty( foundProperty, property)) {
						return false;
					}
					iterator.remove();
				}
			}
		}
		if (testProperties.size() != 0) {
			Assert.fail("not all expected properties found");
			for (Property property : testProperties) {
				System.out.println("missing : " + property.getName());
			}
			return false;
		}
		return true;
	}
	
	default Profile createProfile( String id, Activation activation, Collection<Property> properties, Collection<Repository> repositories, Collection<Repository> pluginRepositories) {
		Profile profile = Profile.T.create();
		profile.setId( id);
		profile.setActivation(activation);
		profile.getProperties().addAll(properties);
		if (repositories != null) 
			profile.getRepositories().addAll(repositories);
		if (pluginRepositories != null) 
			profile.getPluginRepositories().addAll(pluginRepositories);
		
		return profile;
	}
	
	default boolean validateProfile( Profile found, Profile expected, List<String> propertiesToIgnore) {
		String context = "Profile";
		if (!compare ( context + " expected id [%s], but found [%s]", expected.getId(), found.getId())) {
			return false;
		}
		if (!validateActivation(found.getActivation(), expected.getActivation())) {
			return false;
		}
		if (!validateRepositories(found.getRepositories(), expected.getRepositories())) {
			return false;
		}
		if (!validateRepositories(found.getPluginRepositories(), expected.getPluginRepositories())) {
			return false;
		}
		if (!validateProperties( found, expected.getProperties(), propertiesToIgnore)) {
			return false;
		}
		return true;
	}
	
	default boolean validateProfiles( Collection<Profile> foundProfiles, Collection<Profile> expectedProfiles, List<String> propertiesToIgnore) {
		
		if (foundProfiles.size() != expectedProfiles.size()) {
			Assert.fail("expected [" + expectedProfiles.size() + "] profiles, but found [" + foundProfiles.size() + "]");
			return false;
		}
		List<Profile> testProfiles = new ArrayList<>( expectedProfiles);
		Iterator<Profile> iterator = testProfiles.iterator();
		while (iterator.hasNext()) {
			Profile profile = iterator.next();
			for (Profile foundProfile : foundProfiles) {
				if (foundProfile.getId().equals( profile.getId())) {
					if (!validateProfile( foundProfile, profile, propertiesToIgnore)) {
						return false;
					}
					iterator.remove();
				}
			}
		}
		if (testProfiles.size() != 0) {
			Assert.fail("not all expected profiles found");
			for (Profile profile : testProfiles) {
				System.out.println( "missing : " + profile.getId());
			}
			return false;
		}
		return true;
	}
	boolean validate(Settings settings);
}
