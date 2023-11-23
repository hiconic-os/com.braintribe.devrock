// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.locks.Lock;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.json.genericmodel.GenericModelJsonStringCodec;
import com.braintribe.model.ravenhurst.data.RavenhurstMainDataContainer;
import com.braintribe.utils.IOTools;

/**
 * persistence expert to read/write {@link RavenhurstMainDataContainer} data from the local repository 
 * @author Pit
 *
 */
public class RavenhurstPersistenceExpertForMainDataContainer {
	private static final String RAVENHURST_MAIN_OBSOLETE_CONTAINER = ".updated.main";
	private static final String RAVENHURST_MAIN_REWORKED_CONTAINER = ".updateinfo.main";
	private LocalRepositoryLocationProvider localRepositoryLocationProvider;
	private GenericModelJsonStringCodec<RavenhurstMainDataContainer> codec = new GenericModelJsonStringCodec<RavenhurstMainDataContainer>();
	private LockFactory lockFactory;
	
	@Configurable @Required
	public void setLockFactory(LockFactory lockFactory) {
		this.lockFactory = lockFactory;
	}
	
	@Configurable @Required
	public void setLocalRepositoryLocationProvider(LocalRepositoryLocationProvider localRepositoryLocationProvider) {
		this.localRepositoryLocationProvider = localRepositoryLocationProvider;
	}
	
	

	/**
	 * find and load contents of ravenhurst's main container file 
	 * @return - {@link RavenhurstMainDataContainer}
	 * @throws RavenhurstException -
	 */
	public RavenhurstMainDataContainer decode() throws RavenhurstException{

		String localRepository;
		try {
			localRepository = localRepositoryLocationProvider.getLocalRepository(null);
		} catch (RepresentationException e) {
			String msg ="cannot find local ravenhurst main container file [" + RAVENHURST_MAIN_REWORKED_CONTAINER +"]";
			throw new RavenhurstException( msg, e);
		}
		File file = new File( localRepository, RAVENHURST_MAIN_REWORKED_CONTAINER);
		// try to load the reworked file (for this version of MC upwards) 
		if (file.exists()) {
			return decode( file);
		}
		else {
			// try to load the old file as older MC's have
			file = new File( localRepository, RAVENHURST_MAIN_OBSOLETE_CONTAINER);
			if (file.exists()) {
				return decode( file);
			}
			else {
				// pristine : create empty one 
				return RavenhurstMainDataContainer.T.create();
			}
		}
	}



	private RavenhurstMainDataContainer decode(File file) throws RavenhurstException {
		// decode
		String contents;
		Lock semphore = lockFactory.getLockInstance(file).writeLock();
		try {
			semphore.lock();
			contents = IOTools.slurp(file, "UTF-8");
		} catch (IOException e) {
			String msg = "cannot load contents of ravenhurst main container file [" + file.getAbsolutePath() + "]";
			throw new RavenhurstException(msg, e);
		}
		finally {
			semphore.unlock();
		}
		RavenhurstMainDataContainer container;
		try {
			container = codec.decode(contents);
		} catch (CodecException e) {
			String msg = "cannot decode contents of ravenhurst main container file [" + file.getAbsolutePath() + "]";
			throw new RavenhurstException(msg, e);
		}
		return container;
	}
	
	/**
	 * @param container - {@link RavenhurstMainDataContainer}
	 */
	public void encode( RavenhurstMainDataContainer container) throws RavenhurstException {
		if (container.getUrlToLastAccessMap().size() == 0) {
			return;
		}
		container.setLastAccess( new Date());
		String contents;
		try {
			contents = codec.encode(container);
		} catch (CodecException e) {
			String msg = "cannot encode ravenhurst main container data";
			throw new RavenhurstException(msg, e);
		}
		String localRepository;
		try {
			localRepository = localRepositoryLocationProvider.getLocalRepository(null);
		} catch (RepresentationException e) {
			String msg ="cannot determine place for local ravenhurst main container file [" + RAVENHURST_MAIN_REWORKED_CONTAINER +"]";
			throw new RavenhurstException( msg, e);
		}
		Lock semaphore = null;
		File file = new File( localRepository, RAVENHURST_MAIN_REWORKED_CONTAINER);
		try {
			semaphore = lockFactory.getLockInstance(file).writeLock();
			semaphore.lock();
			IOTools.spit(file, contents, "UTF-8", false);
		} catch (IOException e) {
			String msg ="cannot write local ravenhurst main container file [" + file.getAbsolutePath() +"]";
			throw new RavenhurstException( msg, e);
		}
		finally {
			semaphore.unlock();
		}
	}
		
}
