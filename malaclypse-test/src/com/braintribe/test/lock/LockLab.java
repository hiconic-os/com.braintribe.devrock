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
package com.braintribe.test.lock;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.AtomicFilesystemLock;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemLock;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.UuidFilesystemLock;

public class LockLab {
	private  File contents = new File( "res/lockLab/contents");
	private File fileToLock = new File( contents, "fileToLock");

	private int max = 100;
	private int wait = 10;
	private int threshold = 10;
	private File lockFile = new File( contents, "fileToLock.lck");
	private boolean verbose = false;
	
	@Before
	public void onBefore() {
		if (lockFile.exists()) {
			lockFile.delete();
		}
	}
	
	public class UuidRunner implements Runnable {
		private int i;
		private int w;
		private boolean verbose;
		
		public UuidRunner( int i, int w, boolean verbose ) {
			this.i = i;
			this.w = w;
			this.verbose = verbose;
		}
		
		@Override
		public void run() {
			Lock lock = new UuidFilesystemLock(fileToLock, w);
			try {
				lock.lock();
				if (verbose)
					System.out.println("runner [" + i + "] has lock");
			}
			finally {
				if (verbose)
					System.out.println("runner [" + i + "] is unlocking");
				lock.unlock();
				if (verbose)
					System.out.println("runner [" + i + "] has unlocked");
			}						
		}				
	}
	
	public class FileRunner implements Runnable {
		private int i;
		private int w;
		private boolean verbose;
		
		public FileRunner( int i, int w, boolean verbose ) {
			this.i = i;
			this.w = w;
			this.verbose = verbose;
		}
		
		@Override
		public void run() {
			Lock lock = new FilesystemLock(fileToLock, w);
			try {
				lock.lock();
				if (verbose)
					System.out.println("runner [" + i + "] has lock");
			}
			finally {
				if (verbose)
					System.out.println("runner [" + i + "] is unlocking");
				lock.unlock();
				if (verbose)
					System.out.println("runner [" + i + "] has unlocked");
			}						
		}				
	}
	
	public class AtomicFileRunner implements Runnable {
		private int i;
		private int w;
		private boolean verbose;
		
		public AtomicFileRunner( int i, int w, boolean verbose ) {
			this.i = i;
			this.w = w;
			this.verbose = verbose;
		}
		
		@Override
		public void run() {
			Lock lock = new AtomicFilesystemLock(fileToLock, w);
			try {
				lock.lock();
				if (verbose)
					System.out.println("runner [" + i + "] has lock");
			}
			finally {
				if (verbose)
					System.out.println("runner [" + i + "] is unlocking");
				lock.unlock();
				if (verbose)
					System.out.println("runner [" + i + "] has unlocked");
			}						
		}				
	}
	
	@Test
	public void testUuidRunner() {
		ExecutorService executor = Executors.newFixedThreadPool( max);
		long before = System.nanoTime();
		for (int i = 0; i < max; i++) {
			Runnable runnable = new UuidRunner( i, wait, verbose);
			executor.execute(runnable);				
		}
		long after = System.nanoTime(); 
		executor.shutdown();
		while (!executor.isTerminated()) {
			;
		}
		if (lockFile.exists()) {
			Assert.fail( "lock file [" + lockFile.getAbsolutePath() + "] still exists");
		}
		double diff = (after - before) / 1E6;
		System.out.println("[" + max + "] UUID runners needed [" + diff + "] ms to run, i.e. [" + diff / max + "] ms per lock");
	}
	
	@Test
	public void testFileRunner() {
		ExecutorService executor = Executors.newFixedThreadPool( max);
		long before = System.nanoTime();
		for (int i = 0; i < max; i++) {
			Runnable runnable = new FileRunner( i, wait, verbose);
			executor.execute(runnable);				
		}
		long after = System.nanoTime();
		executor.shutdown();
		while (!executor.isTerminated()) {
			;
		}
		if (lockFile.exists()) {
			Assert.fail( "lock file [" + lockFile.getAbsolutePath() + "] still exists");
		}
		double diff = (after - before) / 1E6;
		System.out.println("[" + max + "] file runners needed [" + diff + "] ms to run, i.e. [" + diff / max + "] ms per lock");
		
	}
	
	@Test
	public void testAtomicFileRunner() {
		ExecutorService executor = Executors.newFixedThreadPool( max);
		long before = System.nanoTime();
		for (int i = 0; i < max; i++) {
			Runnable runnable = new AtomicFileRunner( i, wait, verbose);
			executor.execute(runnable);				
		}
		long after = System.nanoTime();
		executor.shutdown();
		while (!executor.isTerminated()) {
			;
		}
		if (lockFile.exists()) {
			Assert.fail( "lock file [" + lockFile.getAbsolutePath() + "] still exists");
		}
		double diff = (after - before) / 1E6;
		System.out.println("[" + max + "] atomic runners needed [" + diff + "] ms to run, i.e. [" + diff / max + "] ms per lock");
	}


}
