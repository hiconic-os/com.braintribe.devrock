// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation;

import java.util.List;

import com.braintribe.model.artifact.Identification;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.model.ravenhurst.Part;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstRequest;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstResponse;

/**
 * interface for interrogation of repositories<br/>
 * uses {@link RavenhurstBundle}, that contains the {@link RavenhurstRequest} and the {@link RavenhurstResponse} to
 * access the repository.<br/>
 * Repositories without Ravenhurst functionality (pure maven for instance) can be handled by specific instances of this interface.  
 * @author Pit
 *
 */
public interface RepositoryInterrogationClient {
	/**
	 * interrogate a ravenhurst capable repository using the information passed, and return its response
	 * @param request - the {@link RavenhurstRequest} that contains all pertinent information 
	 * @return - the response as a {@link RavenhurstResponse}
	 * @throws RepositoryInterrogationException -
	 */
	RavenhurstResponse interrogate( RavenhurstRequest request) throws RepositoryInterrogationException;
	
	/**
	 * interrogate RH about the index - aka a 'date-less' request. 
	 * @param request
	 * @return
	 * @throws RepositoryInterrogationException
	 */
	RavenhurstResponse extractIndex( RavenhurstRequest request) throws RepositoryInterrogationException;
	
	/**
	 * extract a list of {@link Part} that represent the contents of a remote repository's directory (of an versioned artifact) 
	 * @param bundle - the {@link RavenhurstBundle} that contains all relevant data 
 	 * @param artifact - the {@link Artifact} 
	 * @return - a {@link List} of {@link Part}, as found in the directory 
	 * @throws RepositoryInterrogationException -
	 */
	List<Part> extractPartList( RavenhurstBundle bundle, Artifact artifact) throws RepositoryInterrogationException;
	
	/**
	 * extracts a list of version of a given artifact (the version member of the artifact is ignored, may be null)
	 * @param bundle - the {@link RavenhurstBundle} with the pertinent information 
	 * @param artifact - the {@link Artifact} of which groupId & artifactId is taken 
	 * @return - a {@link List} of {@link String} with all the version directory names found 
	 * @throws RepositoryInterrogationException -
	 */
	List<String> extractVersionList( RavenhurstBundle bundle, Identification artifact) throws RepositoryInterrogationException;
			
}
