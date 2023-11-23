// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.devrock.mc.core.lock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.ManagedFilesystemLockSupplier;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.utils.FileTools;

public class ManagedFilesystemLockTest implements HasCommonFilesystemNode{
	
	
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("locks");
		input = pair.first;
		output = pair.second;
			
	}
	@Test
	public void testLockTouchSelectivity() {
		File lockDir = getLockDir();
		
		System.out.println("lock-folder: " + lockDir);
		
		
		File file1 = new File(lockDir, "test1");
		File file2 = new File(lockDir, "test2");
		
		File lockFile1 = lockFileFor(file1);
		File lockFile2 = lockFileFor(file2);
		
		ManagedFilesystemLockSupplier lockSupplier = createLockSupplier();

		try {
			Lock lock1 = lockSupplier.get(file1).readLock();
			lock1.lock();
			Thread.sleep(500L);
			Lock lock2 = lockSupplier.get(file2).readLock();
			lock2.lock();

			long s = System.currentTimeMillis();
			
			List<Pair<Long, Long>> sequence = new ArrayList<>();
			
			while (System.currentTimeMillis() - s < 10_000) {
				try {
					Thread.sleep(200L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				long t1 = lockFile1.lastModified();
				long t2 = lockFile2.lastModified();
				
				Pair<Long, Long> current = Pair.of(t1, t2);
				
				if (sequence.isEmpty())
					sequence.add(current);
				else {
					Pair<Long, Long> last = sequence.get(sequence.size() - 1);
					
					if (!last.equals(current)) {
						sequence.add(current);
					}
				}
			}
			
			// check sequence
			Boolean last = null;
			for (Pair<Long, Long> entry: sequence) {
				boolean gt = entry.first() > entry.second();
				
				if (last == null)
					last = gt;
				else {
					if (last == gt)
						Assertions.fail("unexpected change of touch dates");
					else 
						last = gt;
				}
			}
		
		}
		catch (InterruptedException e) {
			// noop
		}
		finally {
			try {
				FileTools.deleteDirectoryRecursively(lockDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			lockSupplier.preDestroy();
		}
	}

	
	@Test
	public void testStaleLock() {
		File lockDir = getLockDir();
		
		System.out.println("lock-folder: " + lockDir);
		
		
		File file = new File(lockDir, "test");

		try {
			ManagedFilesystemLockSupplier lockSupplier = createLockSupplier();
			Lock readLock = lockSupplier.get(file).readLock();
			readLock.lock();
			
			lockSupplier.preDestroy();
			
			Thread.sleep(2000L);
			
			ManagedFilesystemLockSupplier freshLockSupplier = createLockSupplier();
			
			readLock = freshLockSupplier.get(file).readLock();
			readLock.lock();
		}
		catch (InterruptedException e) {
			// noop
		}
		finally {
			try {
				FileTools.deleteDirectoryRecursively(lockDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private ManagedFilesystemLockSupplier createLockSupplier() {
		ManagedFilesystemLockSupplier lockSupplier = new ManagedFilesystemLockSupplier();
		lockSupplier.setTouchIntervalInMs(1000);
		lockSupplier.setTouchWorkerIntervalInMs(100);
		return lockSupplier;
	}
	
	@Test
	public void testLock() {
		File lockDir = getLockDir();
		
		System.out.println("lock-folder: " + lockDir);
		
		ManagedFilesystemLockSupplier lockSupplier = createLockSupplier();
		
		try {
			int lockCount = 10;

			List<Lock> locks = new ArrayList<>();
			List<File> lockFiles = new ArrayList<>();
			for (int i = 0; i < lockCount; i++) {
				File file = new File(lockDir, String.valueOf(i));
				File lockFile = lockFileFor(file);
				lockFiles.add(lockFile);
				Lock lock = lockSupplier.get(file).readLock();
				locks.add(lock);
				
				lock.lock();
				
				Assertions.assertThat(lock.tryLock()).isFalse();
			}
			
			try {
				System.out.println("inside: " + lockSupplier.getCurrentlyManagedLockFiles());
				
				long s = System.currentTimeMillis();
				
				while (System.currentTimeMillis() - s < 20_000) {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					long time = System.currentTimeMillis();
					for (File lockFile: lockFiles) {
						long delta = time - lockFile.lastModified();
						if (delta > 1500)
							Assertions.fail("lock touch age is not as expected: " + delta + "ms");
					}
				}
			}
			finally {
				locks.forEach(Lock::unlock);
			}
			
			Assertions.assertThat(lockSupplier.getCurrentlyManagedLockFiles()).isEmpty();
		}
		finally {
			try {
				FileTools.deleteDirectoryRecursively(lockDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
			lockSupplier.preDestroy();
		}
	}


	private File getLockDir() {
		String uuid = UUID.randomUUID().toString();
		String name = "managed-fs-lock-test-" + uuid;
		File lockDir = new File( output, name);
		return lockDir;
	}


	private File lockFileFor(File file) {
		return new File(file.getAbsolutePath() + ".lck");
	}
}
