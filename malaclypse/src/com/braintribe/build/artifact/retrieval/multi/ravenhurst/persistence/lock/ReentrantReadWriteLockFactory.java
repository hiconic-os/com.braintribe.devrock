package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * a {@link LockFactory} implementation that returns {@link ReentrantReadWriteLock}
 * 
 * @author pit
 *
 */
public class ReentrantReadWriteLockFactory implements LockFactory {

	@Override
	public ReadWriteLock getLockInstance(File fileToLock) {
		return new ReentrantReadWriteLock();
	}

}
