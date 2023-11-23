package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;

/**
 * a persistence expert for indexes built via Ravenhurst responses
 * @author pit 
 *
 */
public interface RavenhurstRepositoryIndexPersistenceExpert {

	/**
	 * @param factory - set the lock factory to be used
	 */
	void setLockFactory( LockFactory factory);
	
	/**
	 * load the index 
	 * @param file - the repository specific file 
	 * @return - a {@link Set} of the artifact-groups
	 */
	Set<String> decode( File file);
	
	/**
	 * dump the index 
	 * @param file - the repository specific file 
	 * @param index - a {@link Set} of the artifact-groups
	 */
	void encode( File file, Collection<String> index);
	
	/**
	 * update the index - by appending
	 * @param file - the repository specific file
	 * @param index - a {@link Set} of the artifact-groups
	 */
	void update( File file, Collection<String> index);
	
}
