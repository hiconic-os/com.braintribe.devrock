// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope;

import java.util.Date;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhurstPersistenceExpertForMainDataContainer;
import com.braintribe.model.ravenhurst.data.RavenhurstMainDataContainer;

/**
 * abstracted access to the {@link RavenhurstMainDataContainer} via the encapsulated {@link RavenhurstPersistenceExpertForMainDataContainer}
 * within the {@link RavenhurstScope}
 * @author Pit
 *
 */
public interface RavenhurstMainDataHandler {
	/**
	 * update the timestamp of a url based repository
	 * @param url - the url the functionally identifies the repository 
	 * @param date - the {@link Date} to store 
	 * @throws RavenhurstException -
	 */
	void updateTimestamp( String url, Date date) throws RavenhurstException;
	
	/**
	 * returns the last {@link Date} a repository was queried
	 * @param url - the url to get the timestamp of 
	 * @return - the {@link Date} stored or null if nothing's stored.
	 * @throws RavenhurstException -
	 */
	Date getUpdateTimestamp( String url) throws RavenhurstException;
	
	/**
	 * stores the currently accumulated information 
	 * @throws RavenhurstException -
	 */
	void persistData() throws RavenhurstException;
}
