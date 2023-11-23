// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry;

import java.util.Collection;
import java.util.Set;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.model.ravenhurst.data.RavenhurstMainDataContainer;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

/**
 * a registry for the ravenhurst / maven data <br/>
 * any first time access will automatically update all container information, and from then on return cached
 * data (unless clear's called).<br/>
 * automatically locates the current location of the local repository (via {@link MavenSettingsReader}) and updates using the 
 * current {@link RavenhurstScope}.
 * 
 * @author pit
 *
 */
public interface RavenhurstPersistenceRegistry {	
	/**
	 * gets the main data container (ravehurst specific : information about all remote repositories linked to local repo)
	 * @return - the {@link RavenhurstMainDataContainer}
	 */
	RavenhurstMainDataContainer getRavenhurstMainDataContainer();
	void persistRavenhustMainDataContainer();
	
	void persistRavenhurstBundle( RavenhurstBundle bundle);
	
	/**
	 * sets the parameterized current reader 
	 * @param reader - the {@link MavenSettingsReader} to use
	 */
	void setLocalRepositoryLocationProvider( LocalRepositoryLocationProvider localRepositoryLocationProvider);
	void setLockFactory( LockFactory lockFactory);
	
	
	/**
	 * clears the cached data  
	 */
	void clear();
	
	void updateRavenhurstIndexPersistence(RavenhurstBundle bundle, Collection<String> touchedGroups);
	void persistRavenhurstIndex(RavenhurstBundle bundle, Collection<String> touchedGroups);
	boolean existsRavenhurstIndexPersistence( RavenhurstBundle bundle);
	Set<String> loadRavenhurstIndex( RavenhurstBundle bundle);
}
