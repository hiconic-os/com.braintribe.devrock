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
