// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * a {@link LockFactory} that returns a {@link ReadWriteLock} compatible semaphore-style lock,
 * the {@link AtomicFilesystemLock}
 * @author pit
 *
 */
public class FilesystemSemaphoreLockFactory implements LockFactory {

	public  ReadWriteLock getLockInstance( File file) {
		return new ReadWriteLock() {
			@Override
			public Lock writeLock() {
				return new AtomicFilesystemLock(file);
			}
			
			@Override
			public Lock readLock() {
				return new AtomicFilesystemLock(file);
			}
		};
	}
	
}
