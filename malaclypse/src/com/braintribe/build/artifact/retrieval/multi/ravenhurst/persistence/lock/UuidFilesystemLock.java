// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

/**
 * a simple "binary semaphore" based lock implementation, where the semaphore is represented by a locking file, based on the {@link Lock} interface. 
 * 
 * @author pit
 *
 */
public class UuidFilesystemLock implements Lock{
	private static Logger log = Logger.getLogger(UuidFilesystemLock.class);
	private Date date = new Date();
	private File fileToLock;
	private File semaphore;
	private long waitInMillis = 10;	
	
	/**
	 * create the lock instance for the file passed 
	 * @param file - the file to lock 
	 */
	public UuidFilesystemLock( File file) {
		fileToLock = file;
		semaphore = new File( file.getAbsolutePath() + ".lck");
	}
	/**
	 * create the lock for the file passed, and wait for the specified time while unsuccessfully trying
	 * @param file - the {@link File} to lock
	 * @param waitInMillis - the milliseconds as {@link Long} to wait 
	 */
	public UuidFilesystemLock(File file, long waitInMillis) {
		fileToLock = file;
		semaphore = new File( file.getAbsolutePath() + ".lck");
		this.waitInMillis = waitInMillis;
	
	}
	
	private boolean semaphoreExists() {
		try {
			Files.createFile( semaphore.toPath());
			return false;
		} catch (IOException e1) {			
		}					
		return true;
		
	}
	
	@Override
	public void lock()  {
		semaphore.getParentFile().mkdirs();
		UUID uuid = UUID.randomUUID();	
		boolean acquired = false;
		do {
			if (!semaphoreExists()) {
				try {
					IOTools.spit(semaphore, toString( uuid), "UTF-8", false);
				} catch (IOException e) {
					String msg="cannot write lock for [" + fileToLock.getAbsolutePath() + "]";
					log.error(msg,e);					
				}
				try {
					Thread.sleep( waitInMillis);
				} catch (InterruptedException e) {
					String msg="interrupted, yet still acquiring lock on [" + fileToLock.getAbsolutePath() + "]";
					log.debug(msg,e);
				}
				String contents = null;
				try {
					contents = IOTools.slurp(semaphore, "UTF-8");
				} catch (IOException e) {
					String msg="cannot read back lock for [" + fileToLock.getAbsolutePath() + "]";
					log.error(msg,e);					
				}
				if (contents != null) {
					String check = contents.substring( 0, contents.indexOf( '#'));
					if (check.equalsIgnoreCase( uuid.toString())) {
						acquired = true;
					}
				}
			}
			if (!acquired) {
				try {
					Thread.sleep( waitInMillis);
				} catch (InterruptedException e) {
					String msg="interrupted, yet still acquiring lock on [" + fileToLock.getAbsolutePath() + "]";
					log.debug(msg,e);
				}			
			}			
		} while (!acquired);
	}
	
	@Override
	public void unlock() {
		if (semaphore.exists()) {
			try {
				Files.delete( semaphore.toPath());
			} catch (IOException e) {
				String msg="cannot remove lock for [" + fileToLock.getAbsolutePath() + "]";
				log.error(msg, e);
			}
			
			
		}
	}
		
	public String toString(UUID uuid) {
		return uuid.toString() + "#semaphore for [" + fileToLock.getName() + "] @" + date.toString();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		while (semaphore.exists()) {
			try {
				Thread.sleep( waitInMillis);
			} catch (InterruptedException e) {
				return;
			}
		}
		try {
			IOTools.spit(semaphore, toString(), "UTF-8", false);
		} catch (IOException e) {
			String msg="cannot write lock for [" + fileToLock.getAbsolutePath() + "]";
			log.error(msg,e);
		}
		
	}

	@Override
	public Condition newCondition() {	
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public boolean tryLock() {
		if (semaphore.exists()) {
			return false;
		}
		try {
			IOTools.spit(semaphore, toString(), "UTF-8", false);
			return true;
		} catch (IOException e) {
			String msg="cannot write lock for [" + fileToLock.getAbsolutePath() + "]";
			log.error(msg,e);
		}
		return false;
		
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		long deadTimespan = unit.toNanos(time);
		long start = System.nanoTime();
		while (semaphore.exists()) {
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
		try {
			IOTools.spit(semaphore, toString(), "UTF-8", false);
			return true;
		} catch (IOException e) {
			String msg="cannot write lock for [" + fileToLock.getAbsolutePath() + "]";
			log.error(msg,e);
		}
		return false;
	}
	
	
}
