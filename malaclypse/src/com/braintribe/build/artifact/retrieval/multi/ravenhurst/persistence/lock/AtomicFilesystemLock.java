// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import com.braintribe.logging.Logger;

/**
 * a simple "binary semaphore" based lock implementation, where the semaphore is represented by a locking file, based on the {@link Lock} interface. 
 * 
 * @author pit
 *
 */
public class AtomicFilesystemLock implements Lock{
	private static Logger log = Logger.getLogger(AtomicFilesystemLock.class);
	private Date date = new Date();
	private File fileToLock;
	private File semaphore;
	private long waitInMillis = 100;
	private int maxTriesBeforeFail = 100;
	
	/**
	 * create the lock instance for the file passed 
	 * @param file - the file to lock 
	 */
	public AtomicFilesystemLock( File file) {
		fileToLock = file;
		semaphore = new File( file.getAbsolutePath() + ".lck");
	}
	/**
	 * create the lock for the file passed, and wait for the specified time while unsuccessfully trying
	 * @param file - the {@link File} to lock
	 * @param waitInMillis - the milliseconds as {@link Long} to wait 
	 */
	public AtomicFilesystemLock(File file, long waitInMillis) {
		fileToLock = file;
		semaphore = new File( file.getAbsolutePath() + ".lck");
		this.waitInMillis = waitInMillis;
	}
	
	private boolean semaphoreExists() {
		try {
			Path path = semaphore.toPath();
			Files.createDirectories( semaphore.getParentFile().toPath());
			Files.createFile( path);
			return false;
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				String msg="cannot write lock for [" + fileToLock.getAbsolutePath() + "]";
				log.debug(msg,e);
			}
		}					
		return true;
		
	}
	
	@Override
	public void lock() {
		int tries = 0;
		while (semaphoreExists()) {
			try {
				Thread.sleep( waitInMillis);
			} catch (InterruptedException e) {
				String msg="interrupted, yet still acquiring lock on [" + fileToLock.getAbsolutePath() + "]";
				log.debug(msg,e);
			}
			// 
			tries++;
			if (tries > maxTriesBeforeFail) {
				String msg="giving up trying to acquire lock on file ["+ fileToLock.getAbsolutePath() + "]";
				throw new IllegalStateException( msg);
			}
		}	
	}
	@Override
	public void unlock() {
		if (semaphore.exists()) {
			semaphore.delete();
		}
	}
	@Override
	public String toString() {
		return "Semaphore for [" + fileToLock.getName() + "] @" + date.toString();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		int tries = 0;
		while (semaphoreExists()) {
			Thread.sleep( waitInMillis);
			// 
			tries++;
			if (tries > maxTriesBeforeFail) {
				String msg="giving up trying to acquire lock on file ["+ fileToLock.getAbsolutePath() + "]";
				throw new IllegalStateException( msg);
			}
		}
	}

	@Override
	public Condition newCondition() {	
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public boolean tryLock() {
		return !semaphoreExists();	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		long deadTimespan = unit.toNanos(time);
		long start = System.nanoTime();
		
		while (semaphoreExists()) {
			try {
				long now = System.nanoTime();
				if (now - start > deadTimespan)
					return false;
				Thread.sleep( waitInMillis);
			} catch (InterruptedException e) {
				String msg="interrupted, yet still acquiring lock on [" + fileToLock.getAbsolutePath() + "]";
				log.debug(msg,e);
			}
		}
		
		return true;
	}
	
	
}
