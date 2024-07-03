// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.devrock.mc.core.commons;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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
 * @author dirk
 *
 */
class ManagedFilesystemLock implements Lock{
	private static Logger log = Logger.getLogger(ManagedFilesystemLock.class);
	private Date date = new Date();
	private File fileToLock;
	private Path semaphore;
	private long waitInMillis = 100;
	// private int maxTriesBeforeFail = 100;
	private ManagedLockFiles monitoredLockFiles;
	
	/**
	 * create the lock instance for the file passed 
	 * @param file - the file to lock 
	 */
	public ManagedFilesystemLock(ManagedLockFiles monitoredLockFiles, File file) {
		this.monitoredLockFiles = monitoredLockFiles;
		fileToLock = file;		
		semaphore = new File(file.getAbsolutePath() + ".lck").toPath();
	}
	/**
	 * create the lock for the file passed, and wait for the specified time while unsuccessfully trying
	 * @param file - the {@link File} to lock
	 * @param waitInMillis - the milliseconds as {@link Long} to wait 
	 */
	public ManagedFilesystemLock(ManagedLockFiles monitoredLockFiles, File file, long waitInMillis) {
		this.monitoredLockFiles = monitoredLockFiles;
		fileToLock = file;
		semaphore = new File(file.getAbsolutePath() + ".lck").toPath();
		this.waitInMillis = waitInMillis;
	}
	
	private boolean semaphoreExists() {
		while (true) {
			
			try {
				Path folder = semaphore.getParent();
				if (!Files.exists(folder))
					Files.createDirectories(folder);
			}
			catch (IOException e) {
				throw new UncheckedIOException("Could not create directory for lock file: " + semaphore, e);
			}
			
			try {
				Files.createFile( semaphore);
				monitoredLockFiles.add(semaphore);
				return false;
			} catch (FileAlreadyExistsException e) {
				try {
					long lastModified = Files.getLastModifiedTime(semaphore).toMillis();
					long lockIdleTimeSpan = System.currentTimeMillis() - lastModified;
					
					if (monitoredLockFiles.lockTimeToLiveInMs() < lockIdleTimeSpan) {
						try {
							Files.delete(semaphore);
							continue;
						} catch (NoSuchFileException e1) {
							continue;
						} catch (IOException e2) {
							throw new UncheckedIOException("Error while deleting stale lock file: " + semaphore, e2);
						}
					}
					
					return true;
					
				} catch (NoSuchFileException e1) {
					// file seems to suddenly have vanished - so try again directly
					continue;
				} catch (IOException e2) {
					throw new UncheckedIOException("Error while checking modified date on lock file: " + semaphore, e2);
				}
			}	
			catch (IOException e) {
				throw new UncheckedIOException("Error while trying to create lock file: " + semaphore, e);
			}
		}
		
	}
	
	@Override
	public void lock() {
		while (semaphoreExists()) {
			try {
				Thread.sleep( waitInMillis);
			} catch (InterruptedException e) {
				String msg="interrupted, yet still acquiring lock on [" + fileToLock.getAbsolutePath() + "]";
				log.debug(msg,e);
			}
		}	
	}
	@Override
	public void unlock() {
		if (Files.exists(semaphore)) {
			monitoredLockFiles.remove(semaphore);
			try {
				Files.delete(semaphore);
			} catch (IOException e) {
				log.error("Error while deleting lock file: " + semaphore, e);
			}
		}
	}
	@Override
	public String toString() {
		return "Semaphore for [" + fileToLock.getName() + "] @" + date.toString();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		while (semaphoreExists()) {
			Thread.sleep( waitInMillis);
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
		long deadTimespan = unit.toMillis(time);
		long start = System.currentTimeMillis();
		
		while (semaphoreExists()) {
			long now = System.currentTimeMillis();
			
			long millisConsumend = now - start;
			
			long leftOver = deadTimespan - millisConsumend;
			
			if (leftOver <= 0)
				return false;

			long wait = Math.min(leftOver, waitInMillis);
			
			Thread.sleep( wait);
		}
		
		return true;
	}
	
	
}
