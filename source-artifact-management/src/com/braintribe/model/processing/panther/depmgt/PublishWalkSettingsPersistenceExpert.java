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
package com.braintribe.model.processing.panther.depmgt;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.AbstractMavenSettingsPersistenceExpert;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Property;
import com.braintribe.model.maven.settings.Repository;
import com.braintribe.model.maven.settings.RepositoryPolicy;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.maven.settings.Settings;
import com.braintribe.model.panther.ArtifactRepository;
import com.braintribe.model.panther.SourceRepository;

/**
 * programmatically creates a {@link Settings} to reflect the configuration 
 * of the publishing run 
 * 
 * @author pit
 *
 */
public class PublishWalkSettingsPersistenceExpert extends AbstractMavenSettingsPersistenceExpert {

	private static final String RAVENHURST_REPOSITORIES = "updateReflectingRepositories";
	private static final String RAVENHURST_URL = "ravenhurst-url-";
	private static final String CENTRAL_URL = "http://repo1.maven.org/maven2";
	private static final String CENTRAL_ID = "central";
	private static final String UPDATE_POLICY_NEVER = "never";
	
	private String localRepositoryLocation;
	private Supplier<Stream<ArtifactRepository>> lookupRepositoriesSupplier = () -> Stream.of();

	@Configurable
	public void setSourceRepository(SourceRepository sourceRepository) {
		this.lookupRepositoriesSupplier = () -> sourceRepository.getLookupRepositories().stream();
	}
	
	@Configurable
	public void setLookupRepositoriesSupplier(Supplier<Stream<ArtifactRepository>> lookupRepositoriesSupplier) {
		this.lookupRepositoriesSupplier = lookupRepositoriesSupplier;
	}
	
	@Configurable @Required
	public void setLocalRepositoryLocation(String localRepositoryLocation) {
		this.localRepositoryLocation = localRepositoryLocation;
	}
	
	@Override
	public Settings loadSettings() throws RepresentationException {
		Settings settings = Settings.T.create();
		
		// local repository?? 
		settings.setLocalRepository( localRepositoryLocation);
		
		// prime server container 
		
		// prime profile "panther"
		Profile profile = Profile.T.create();
		settings.getProfiles().add(profile);
		
		profile.setId( "panther");
		
		
		// add inhibitor for maven central
		addMavenInhibitors( profile);
		
		List<Server> servers = settings.getServers();
		
		// add entry for each lookup repository
		this.lookupRepositoriesSupplier.get().forEach(r -> {
			createAndAttachRepository(servers, profile, r);			
		});
		
		return settings;
	}

	private void addMavenInhibitors(Profile profile) {		
		addMavenInhibitor( profile.getRepositories());		
		addMavenInhibitor( profile.getPluginRepositories());
		
	}

	/**
	 * add a blocker repository entry, so that maven repository won't be used
	 * @param profile - the {@link Profile} to add it to 
	 */
	private void addMavenInhibitor(List<Repository> repositories) {
		Repository repository = Repository.T.create();
		repositories.add(repository);
		
		repository.setId( CENTRAL_ID);
		repository.setUrl( CENTRAL_URL);

		repository.setSnapshots( createPolicy(false, UPDATE_POLICY_NEVER));
		repository.setReleases( createPolicy(false, UPDATE_POLICY_NEVER));
					
	}

	/**
	 * create a repository complex, and add it to the settings/profile
	 * @param servers - the container for the {@link Server}
	 * @param profile - the {@link Profile}
	 * @param lookupRepository - the {@link ArtifactRepository}
	 */
	private void createAndAttachRepository(List<Server> servers, Profile profile, ArtifactRepository lookupRepository) {
		Repository repository = Repository.T.create();
		profile.getRepositories().add(repository);
		
		String lookupRepositoryName = lookupRepository.getName();
		repository.setId( lookupRepositoryName);
		repository.setUrl( lookupRepository.getRepoUrl());

		repository.setSnapshots( createPolicy(false, null));
		String updateReflectionUrl = lookupRepository.getUpdateReflectionUrl();
		if (updateReflectionUrl != null) {		
			repository.setReleases(createPolicy( true, UPDATE_POLICY_NEVER));

			// add ravenhurst url 
			Property property = Property.T.create();
			property.setName(RAVENHURST_URL + lookupRepositoryName);
			property.setRawValue( updateReflectionUrl);
			property.setValue(updateReflectionUrl);			
			profile.getProperties().add( property);

			// add dynamic repo to list  
			updateDynamicRepositoryDeclaration(profile, lookupRepositoryName);
		} 
		else {
			// no update policy for releases, i.e. default == daily?  
			repository.setReleases(createPolicy( true, null));
		}
		
		Server server = createServer( lookupRepositoryName, lookupRepository.getUser(), lookupRepository.getPassword());
		servers.add(server);
	}

	/**
	 * create a repository policy entry 
	 * @param enabled - true if enabled, false if not 
	 * @param update - the update policy to use
	 * @return - a {@link RepositoryPolicy}
	 */
	private RepositoryPolicy createPolicy( boolean enabled, String update) {
		RepositoryPolicy policy = RepositoryPolicy.T.create();		
		policy.setEnabled(enabled);
		if (update != null)
			policy.setUpdatePolicy(update);
		
		return policy;
	}
	
	/**
	 * create a server 
	 * @param id - the id of the {@link Server} (and id of the {@link Repository} it links to 
	 * @param user - the user name 
	 * @param pwd - the password 
	 * @return - the created {@link Server}
	 */
	private Server createServer( String id, String user, String pwd) {
		Server server = Server.T.create();
		server.setId(id);
		server.setUsername(user);
		server.setPassword(pwd);
		return server;
	}
	
	/**
	 * attach dynamic repository's name to property 
	 * @param profile - the {@link Profile} that contains the {@link Property}
	 * @param id - the id of the dynamic repository
	 */
	private void updateDynamicRepositoryDeclaration( Profile profile, String id) {
		// find the property 
		Property property = null;
		for (Property suspect : profile.getProperties()) {
			if (suspect.getName().equalsIgnoreCase( RAVENHURST_REPOSITORIES)) {
				property = suspect;
				break;
			}
		}
		// add 
		if (property == null) {
			property = Property.T.create();
			property.setName( RAVENHURST_REPOSITORIES);
			property.setRawValue( id);
			property.setValue(id);
			profile.getProperties().add(property);
		}
		else { // edit
			String value = property.getValue();
			value += "," + id;
			property.setValue(value);
			property.setRawValue(value);
		}
	}
}
