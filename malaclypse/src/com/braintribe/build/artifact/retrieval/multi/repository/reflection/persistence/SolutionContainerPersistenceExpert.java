// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.json.genericmodel.GenericModelJsonStringCodec;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.ravenhurst.data.RavenhurstSolutionDataContainer;
import com.braintribe.model.ravenhurst.data.RepositoryRole;
import com.braintribe.utils.IOTools;

public class SolutionContainerPersistenceExpert {
	private static Logger log = Logger.getLogger(SolutionContainerPersistenceExpert.class);
	public static final String RAVENHURST_SOLUTION_CONTAINER = ".updated.solution";	
	private static GenericModelJsonStringCodec<RavenhurstSolutionDataContainer> codec = new GenericModelJsonStringCodec<RavenhurstSolutionDataContainer>();

	/**
	 * read the file and return its content (or an empty container if not present)
	 * @param location - {@link File} pointing to the data 
	 * @return - {@link RavenhurstSolutionDataContainer}
	 * @throws RepositoryPersistenceException -
	 */
	public static RavenhurstSolutionDataContainer decode(LockFactory lockFactory, File location) throws RepositoryPersistenceException {
		File file = new File( location, RAVENHURST_SOLUTION_CONTAINER);
		RavenhurstSolutionDataContainer container;
		if (!file.exists()) {
			container = RavenhurstSolutionDataContainer.T.create();
			return container;
		}
		
		// decode
		String contents;
		Lock semaphore = lockFactory.getLockInstance(file).readLock();
		try {
			semaphore.lock();
			contents = IOTools.slurp(file, "UTF-8");
		} catch (IOException e) {
			String msg = "cannot load contents of ravenhurst main container file [" + file.getAbsolutePath() + "]";
			log.error(msg, e);
			throw new RepositoryPersistenceException(msg, e);
		}
		finally {
			semaphore.unlock();
		}
		
		try {
			container = codec.decode(contents);
		} catch (CodecException e) {
			String msg = "cannot decode contents of ravenhurst main container file [" + file.getAbsolutePath() + "]";
			log.error(msg, e);
			throw new RepositoryPersistenceException(msg, e);
		}
		return container;
	}
	
	/**
	 * encode and write the container 
	 * @param container - {@link RavenhurstSolutionDataContainer}
	 * @param location - {@link File} to write to 
	 * @throws RepositoryPersistenceException -
	 */
	public static void encode( LockFactory lockFactory, RavenhurstSolutionDataContainer container, File location) throws RepositoryPersistenceException {
		container.setLastAccess( new Date());
		
		Map<MavenMetaData, RepositoryRole> mavenMetaDataToRepositoryRoleMap = container.getMavenMetaDataToRepositoryRoleMap();
		Map<String, MavenMetaData> urlToMetaDataMap = container.getUrlToMetaDataMap();
		Solution solution = container.getSolution();
		
		String contents;
		try {
			// drop two data sets for now
			container.setMavenMetaDataToRepositoryRoleMap(null);
			container.setUrlToMetaDataMap(null);
			container.setSolution(null);
			contents = codec.encode(container);
		} catch (CodecException e) {
			String msg = "cannot encode ravenhurst main container data";
			log.error(msg);
			throw new RepositoryPersistenceException(msg, e);
		}	
		finally {
			container.setMavenMetaDataToRepositoryRoleMap(mavenMetaDataToRepositoryRoleMap);
			container.setUrlToMetaDataMap(urlToMetaDataMap);
			container.setSolution(solution);
		}
		File file = new File( location, RAVENHURST_SOLUTION_CONTAINER);
		Lock semaphore = lockFactory.getLockInstance(file).writeLock();
		try {
			semaphore.lock();
			file.getParentFile().mkdirs();
			IOTools.spit(file, contents, "UTF-8", false);
		} catch (IOException e) {
			String msg ="cannot write local ravenhurst main container file [" + file.getAbsolutePath() +"]";
			log.error(msg);
			throw new RepositoryPersistenceException( msg, e);
		}
		finally {
			semaphore.unlock();
		}
	}
}
