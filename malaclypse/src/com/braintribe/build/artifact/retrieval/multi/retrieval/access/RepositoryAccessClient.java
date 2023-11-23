// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.retrieval.access;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClient;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.maven.settings.Server;

/**
 * abstraction of the required access to a remote repository. <br/>
 * allows decoupling maven-standard based repos with HTTP communication from 
 * Panther-based repos (which are hopefully smarter) <br/>
 * see {@link RepositoryInterrogationClient} for the analogon (for Ravenhurst access)
 *  
 * @author Pit
 *
 */
public interface RepositoryAccessClient {
	
	/**
	 * just check if the repository can be reached
	 * @param location - the url to check (mostly the main url of a repository) 
	 * @param server - the {@link Server} with the credentials
	 * @return - true if the url could be reached, false otherwise 
	 */
	boolean checkConnectivity( String location, Server server);
	
	/**
	 * extracts the file names from a specific location (in HTML terms, the files listed in a directory)
	 * @param location - the location to extract the files from (in HTML terms, the directory)
	 * @param server - the appropriate {@link Server} as defined by the maven settings 
	 * @return - a {@link List} of {@link String} with the file names found 
	 * @throws RepositoryAccessException -
	 */
	List<String> extractFilenamesFromRepository( String location, Server server) throws RepositoryAccessException;
	
	/**
	 * extracts the sub-directories of a directory (used to find versions of a given artifact) 
	 * @param location - the location of the parent 
	 * @param server - the appropriate {@link Server} as defined by the maven settings 
	 * @return - a {@link List} of {@link String} with the sub-directories found
	 * @throws RepositoryAccessException -
	 */
	List<String> extractVersionDirectoriesFromRepository( String location, Server server) throws RepositoryAccessException;
	
	/**
	 * extracts a file from a repository (in HTTP terms, it downloads it)
	 * @param source - the fully qualified location of the source file
	 * @param target - the fully qualified location of the target file
	 * @param server - the appropriate {@link Server} a defined by maven's settings
	 * @return - the {@link File} that points to the target 
	 * @throws RepositoryAccessException -
	 */
	File extractFileFromRepository( String source, String target, Server server) throws RepositoryAccessException;
	
	/**
	 * extracts the content of a file from a repository (in HTTP terms, downloads the content of a file as a String)
	 * @param source - the fully qualified location of the source file 
	 * @param server - the appropriate {@link Server} a defined by maven's settings
	 * @return - the content of the file as a string 
	 * @throws RepositoryAccessException -
	 */
	String extractFileContentsFromRepository( String source, Server server) throws RepositoryAccessException;
	
	MavenMetaData extractMavenMetaData(LockFactory lockFactory, String source, Server server) throws RepositoryAccessException;
	
	/**
	 * upload a single file
	 * @param server - the appropriate {@link Server} a defined by maven's settings
	 * @param source - the fully qualified location of the source file (file path)
	 * @param target - the fully qualified destination of the target file (url)
	 * @return - true if upload was successful, false otherwise 
	 * @throws RepositoryAccessException -
	 */
	Integer uploadFile( Server server, File source, String target) throws RepositoryAccessException;
	
	/**
	 * @param server - the appropriate {@link Server} a defined by maven's settings 
	 * @param sourceToTargetMap - a {@link Map} of source file path to destination url 
	 * @return - a {@link Map} of source file path to success. 
	 * @throws RepositoryAccessException -
	 */
	Map<File, Integer> uploadFile( Server server, Map<File, String> sourceToTargetMap) throws RepositoryAccessException;
}
