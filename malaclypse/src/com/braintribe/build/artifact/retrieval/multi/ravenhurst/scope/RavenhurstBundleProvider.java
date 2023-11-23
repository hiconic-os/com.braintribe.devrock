// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope;

import java.util.List;
import java.util.Set;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

/**
 * returns a list of {@link RavenhurstBundle} that contain all information about
 * repositories and their (possible) Ravenhurst functionality providers.
 * 
 * @author Pit
 *
 */
public interface RavenhurstBundleProvider {
	/**
	 * return the currently valid {@link RavenhurstBundle}, may be cached with the scope  
	 * @return - a {@link List} of {@link RavenhurstBundle}
	 * @throws RavenhurstException -
	 */
	List<RavenhurstBundle> getRavenhurstBundles() throws RavenhurstException;

	/**
	 * any profile that has its id listed in the set is skipped
	 * @param profileIds - a {@link Set} of {@link String}, denoting a profile's id (see MavenSettingsModel, settings.xml)
	 */
	void setInhibitedProfilesIds( Set<String> profileIds);
	
	/**
	 * any repository that has its id listed in the set is skipped
	 * @param repositoryIds - {@link Set} of {@link String}, denoting a repository's id (see MavenSettingsModel, settings.xml)
	 */
	void setInhibitedRepositoryIds( Set<String> repositoryIds);	
	/**
	 * any repository that has its URL listed in the set is skipped 
	 * @param repositoryUrls - a {@link Set} of {@link String} denoting a repository's URL (may be different repo ids etc) 
	 */
	void setInhibitedRepositoryUrls( Set<String> repositoryUrls);
	
	/**
	 * get the {@link RavenhurstBundle} with the given name 
	 * @param name - the name of the bundle to look for 
	 * @return - the respective {@link RavenhurstBundle} or null if none's found 
	 * @throws RavenhurstException -
	 */
	RavenhurstBundle getRavenhurstBundleByName( String name) throws RavenhurstException;
	/**
	 * get the first matching {@link RavenhurstBundle} that represents this url
	 * @param url - the URL to look for 
	 * @return - the first matching {@link RavenhurstBundle} or null if none's found
	 * @throws RavenhurstException -
	 */
	RavenhurstBundle getRavenhurstBundleByUrl( String url) throws RavenhurstException;
	
}
