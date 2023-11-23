// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection;

import java.util.List;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.CrcValidationLevel;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.info.ArtifactInformation;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

/**
 * a registry that gives access to higher level experts that in turn give access to the relevant repository data for artifacts and solutions, 
 * so that the different resolvers can look them up.<br/>
 * understands maven metadata and keeps the automatically updated - and caches the information, i.e. no update happens if cache isn't cleared.
 * 
 * @author pit
 *
 */
public interface RepositoryReflection extends LockFactory {

	/**
	 * defines the retrieval mode for {@link RepositoryReflection#retrieveInformation(Solution, RetrievalMode)} 
	 * {@link RetrievalMode#passive} - only check what's there,
	 * {@link {@link RetrievalMode#eager} - force an update (and try to resolve the pom)
	 * <br/><b>NOTE: this will query the repository and some repos (Maven central for instance) don't like that too often</b><br/>
	 * 
	 * @author pit
	 *
	 */
	
	/**
	 * acquires an expert to deal with artifact's repository related information<br/>
	 * if necessary, it creates or loads and updates the different persistence files 
	 * @param artifact - the {@link Identification} we're interested in 
	 * @return - an instance of {@link ArtifactReflectionExpert} containing the data 
	 * @throws RepositoryPersistenceException -
	 */
	ArtifactReflectionExpert acquireArtifactReflectionExpert( Identification artifact) throws RepositoryPersistenceException;
	
	/**
	 * acquires an expert to deal with a solution's repository related information<br/>
	 * if necessary, it creates or loads and updates the different persistence files
	 * @param solution - the {@link Solution} we're interested in
	 * @return - an instance of a {@link SolutionReflectionExpert} containing the data 
	 * @throws RepositoryPersistenceException -
	 */
	SolutionReflectionExpert acquireSolutionReflectionExpert( Solution solution) throws RepositoryPersistenceException;
			
	/**
	 * set the current scope 
	 * @param scope - the {@link RavenhurstScope}
	 */
	void setRavenhurstScope( RavenhurstScope scope);
	
	/**
	 * sets the {@link CrcValidationLevel}, default is {@link CrcValidationLevel.warn
	 * @param level - the {@link CrcValidationLevel} to set 
	 */
	void setCrcValidationLevel( CrcValidationLevel level);
	
	/**
	 * if available tells the {@link RepositoryReflection} that it is part of scope and this is the id of it
	 * @param id - the id
	 */
	void setCurrentScopeId(String id);
	
	void setMavenSettingsReader(MavenSettingsReader reader);
			
	/**
	 * clears the cached data  
	 */
	void clear();
	
	/**
	 * releases all bound instances and closes all connections 
	 */
	void closeContext();
	
	/** 
	 * queries the repositories for changes in order to delete the maven-metadata.xml files for the changed artifacts
	 */
	void purgeOutdatedMetadata();
	
	/**
	 * delivers information about an artifact<br/>
	 * notes<br/>
	 * {@link RetrievalMode#eager} will 'enumerate' the involved repositories, 
	 * Maven central for instance doesn't like that.
	 * 
	 * @param solution - the {@link Solution} to retrieve information about
	 * @param mode - either {@link RetrievalMode#passive} or {@link RetrievalMode#eager}  
	 * @return - {@link ArtifactInformation} with the available data
	 */
	ArtifactInformation retrieveInformation( Solution solution, RetrievalMode mode);

	/**
	 * updates the index of this bundle and invalidates maven-metadata.xml of groups found added
	 * @param bundle - the {@link RavenhurstBundle}
	 * @return - a {@link List} of the added groups
	 */
	List<String> correctLocalRepositoryStateOf(RavenhurstBundle bundle);

	/**
	 * updates the index of this bundle and invalidates maven-metadata.xml of groups found added
	 * @param repo - the name of the repo
	 * @return - a {@link List} of the added groups
	 */
	List<String> correctLocalRepositoryStateOf(String repo);

	RepositoryViewResolution getRepositoryViewResolution();
	
	
}
