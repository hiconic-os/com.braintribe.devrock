// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.json.genericmodel.GenericModelJsonStringCodec;
import com.braintribe.model.ravenhurst.data.RavenhurstArtifactRedeployDataContainer;
import com.braintribe.utils.IOTools;

public class RedeployContainerPersistenceExpert {
	public static final String RAVENHURST_ARTIFACT_REDEPLOY_CONTAINER = ".updated.artifact.redeploy";
	private static GenericModelJsonStringCodec<RavenhurstArtifactRedeployDataContainer> codec = new GenericModelJsonStringCodec<RavenhurstArtifactRedeployDataContainer>();	
	private LockFactory lockFactory;
	
	@Configurable @Required
	public void setLockFactory(LockFactory lockFactory) {
		this.lockFactory = lockFactory;
	}
	
	public RavenhurstArtifactRedeployDataContainer decode( File file) throws RepositoryPersistenceException {
		Lock semaphore = lockFactory.getLockInstance(file).readLock();
		try {
			// get lock 
			semaphore.lock();
			String contents = IOTools.slurp(file, "UTF-8");
			RavenhurstArtifactRedeployDataContainer redeployContainer = codec.decode(contents);			
			return redeployContainer;		
		} catch (IOException e) {
			String msg="cannot read redeploy container [" + file.getAbsolutePath() + "]";
			throw new RepositoryPersistenceException(msg, e);
		} catch (CodecException e) {
			String msg="cannot decode redeploy container [" + file.getAbsolutePath() + "]";
			throw new RepositoryPersistenceException(msg, e);
		} 				
		finally {
			semaphore.unlock();
		}
	}
	
	public void encode( RavenhurstArtifactRedeployDataContainer redeployContainer, File redeployFile) throws RepositoryPersistenceException {
		Lock redeploySemaphore = lockFactory.getLockInstance( redeployFile).writeLock();
		try {
			redeploySemaphore.lock();
			String contents = codec.encode(redeployContainer);			
			IOTools.spit( redeployFile, contents, "UTF-8", false);
		} catch (CodecException e) {
			String msg = "cannot encode redeploy map";
			throw new RepositoryPersistenceException(msg, e);
		} catch (IOException e) {
			String msg = "cannot write redeploy map to [" + redeployFile.getAbsolutePath() + "]";
			throw new RepositoryPersistenceException(msg, e);
		}
		finally {
			redeploySemaphore.unlock();
		}
	}

}
