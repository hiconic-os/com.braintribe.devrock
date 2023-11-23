// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence;

import java.io.File;
import java.util.List;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

public interface RavenhurstPersistenceExpertForRavenhurstBundle {

	/**
	 * load all persisted bundles into a list, first one first, last one last
	 * @param file - file to read 
	 * @return - a {@link List} of {@link RavenhurstBundle}
	 * @throws RavenhurstException -
	 */
	List<RavenhurstBundle> decodePerStringSearch(File file) throws RavenhurstException;

	/**
	 * load all persisted bundles into a list, first one first, last one last
	 * @param file - file to read 
	 * @return - a {@link List} of {@link RavenhurstBundle}
	 * @throws RavenhurstException -
	 */
	List<RavenhurstBundle> bulkDecode(File file) throws RavenhurstException;

	/**
	 * dump a list of {@link RavenhurstBundle}s to disk - used after pruning for instance
	 * @param file - the {@link File} to write to
	 * @param bundles - a {@link List} of {@link RavenhurstBundle}
	 * @throws RavenhurstException - 
	 */
	void encode(File file, List<RavenhurstBundle> bundles) throws RavenhurstException;

	/**
	 * appends a {@link RavenhurstBundle} to a file
	 * @param file - the file to write to 
	 * @param bundle - the {@link RavenhurstBundle} to write 
	 * @throws RavenhurstException -
	 */
	void encode(File file, RavenhurstBundle bundle) throws RavenhurstException;
	
	RavenhurstBundle decode( File file) throws RavenhurstException;
	
	void setLockFactory( LockFactory factory);

}
