// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.ravenhurst.data.RepositoryRole;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

/**
 * an expert to handle repository information on the solution level (aka versioned artifact level) <br/>
 * can retrieve parts from the local repo or from the remote repo,
 * and processes update information, by currently marking - in case of a dynamic repo - all
 * parts as redeployed, and - in case of a snapshot repo - but updating both part list and metadata
 * @author pit
 *
 */
public interface SolutionReflectionExpert {

	//static final String SUFFIX_TRANSACTION =".download";
	/**
	 * retrieves the download information of a part 
	 * @param part - the {@link Part} to look for
	 * @param expectedPartName - the expected name of the {@link Part} (enricher may have several possibilities) 
	 * @param target - the location aka target as a string 
	 * @param repositoryRole - the {@link RepositoryRole} that we're acting upon (release or snapshot) 
	 * @return - the {@link PartDownloadInfo} containing url and source.
	 * @throws RepositoryPersistenceException -
	 */
	Collection<PartDownloadInfo> getDownloadInfo( Part part, String expectedPartName, String target, RepositoryRole repositoryRole) throws RepositoryPersistenceException;	

	/**
	 * retrieves the actual file 
	 * @param part - the {@link Part} to retrieve
	 * @param expectedPartName - the expected name of the {@link Part} (enricher may have several possibilities)
	 * @param repositoryRole - the {@link RepositoryRole} that we're acting upon (release or snapshot)
	 * @return - the {@link File}
	 * @throws RepositoryPersistenceException -
	 */
	File getPart( Part part, String expectedPartName, RepositoryRole repositoryRole) throws RepositoryPersistenceException;
	
	/**
	 * retrieves the part's file, yet returns the {@link PartDownloadInfo} and a file with the {@link #SUFFIX_TRANSACTION} suffix
	 * @param candidate - the owning {@link Part} of the file 
	 * @param expectedPartName - the expected name of the {@link Part} (enricher may have several possibilites)
	 * @param repositoryRole - the required {@link RepositoryRole}
	 * @return - a {@link PartDownloadInfo} with all pertinent info (and a pointer to the downloaded file)
	 */
	PartDownloadInfo transactionalGetPart(Part candidate, String expectedPartName, RepositoryRole repositoryRole);
	
	/**
	 * process update information about the bundle 
	 * @param bundle - {@link RavenhurstBundle} to process 
	 */
	void processUpdateInformation( RavenhurstBundle bundle) throws RepositoryPersistenceException;

	/**
	 * Finds all existing part tuples in the configured repositories (doesn't mean they are already downloaded)
	 */
	List<PartTuple> listExistingPartTuples(Solution solution, RepositoryRole repositoryRole);
	
	/**
	 * get a list of all files available from all active remote repositories
	 * @param bundle
	 * @return
	 * @throws RepositoryPersistenceException
	 */
	List<String> retrievePartListFromRemoteRepository( RavenhurstBundle bundle) throws RepositoryPersistenceException;
	
	/**
	 * @return - the respective location within the local repository
	 */
	File getLocation();
}
