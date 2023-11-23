// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.json.genericmodel.GenericModelJsonStringCodec;
import com.braintribe.logging.Logger;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.utils.IOTools;

public class FilesystemBasedPersistenceExpertForRavenhurstBundle implements RavenhurstPersistenceExpertForRavenhurstBundle {
	private static Logger log = Logger.getLogger(FilesystemBasedPersistenceExpertForRavenhurstBundle.class);
	private GenericModelJsonStringCodec<RavenhurstBundle> codec = new GenericModelJsonStringCodec<RavenhurstBundle>();
	private LockFactory lockFactory;
	
	@Configurable @Required
	public void setLockFactory(LockFactory lockFactory) {
		this.lockFactory = lockFactory;
	}
	

	@Override
	public List<RavenhurstBundle> decodePerStringSearch(File file) throws RavenhurstException {
		throw new UnsupportedOperationException("not implemented by this type");
	}

	@Override
	public List<RavenhurstBundle> bulkDecode(File file) throws RavenhurstException {
		throw new UnsupportedOperationException("not implemented by this type");
	}

	@Override
	public void encode(File file, List<RavenhurstBundle> bundles) throws RavenhurstException {
		throw new UnsupportedOperationException("not implemented by this type");
	}

	@Override
	public void encode(File file, RavenhurstBundle bundle) throws RavenhurstException {
		Lock semaphore = lockFactory.getLockInstance(file).writeLock();
		try {
			String contents = codec.encode(bundle);
			semaphore.lock();
			IOTools.spit(file, contents, "UTF-8", false);
		} catch (CodecException e) {
			String msg ="cannot encode contents of bundle to [" + file.getAbsolutePath() + "]";
			log.error( msg, e);
			throw new RavenhurstException(msg, e);
		} catch (IOException e) {
			String msg ="cannot append to file [" + file.getAbsolutePath() + "]";
			log.error( msg, e);
			throw new RavenhurstException(msg, e);
		}
		finally {
			semaphore.unlock();
		}
	}
	
	public RavenhurstBundle decode( File file) throws RavenhurstException {
		Lock semaphore = lockFactory.getLockInstance(file).writeLock();
		try {
			semaphore.lock();
			String contents = IOTools.slurp(file, "UTF-8");
			RavenhurstBundle bundle = codec.decode(contents);
			return bundle;
		} catch (CodecException e) {
			String msg ="cannot encode contents of bundle to [" + file.getAbsolutePath() + "]";
			log.error( msg, e);
			throw new RavenhurstException(msg, e);
		} catch (IOException e) {
			String msg ="cannot read from file [" + file.getAbsolutePath() + "]";
			log.error( msg, e);
			throw new RavenhurstException(msg, e);
		}
		finally {
			semaphore.unlock();
		}
	}

}
