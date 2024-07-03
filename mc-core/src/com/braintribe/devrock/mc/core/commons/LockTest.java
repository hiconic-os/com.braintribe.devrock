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

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * simple test bed for wrapping locks
 * @author pit
 *
 */
public class LockTest {
	public static void main(String[] args) {
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		
		ReadLock readLock = lock.readLock();
		
		readLock.lock();
		
		
		try {
			readLock.lock();
			try {	
				WriteLock writeLock = lock.writeLock();
				writeLock.lock();
				try {
					System.out.println("hallo");
				}
				finally {
					writeLock.unlock();
				}
			}
			finally {
				readLock.unlock();
			}
			
		}
		finally {
			readLock.unlock();
		}
		
	}
}
