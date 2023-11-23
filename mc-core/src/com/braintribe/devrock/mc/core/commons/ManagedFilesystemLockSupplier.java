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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.logging.Logger;

/**
 * supplier for a filesystem lock (a semaphore) 
 * @author pit
 * @author dirk
 */
public class ManagedFilesystemLockSupplier implements Function<File, ReadWriteLock>, DestructionAware {
	private static final Logger log = Logger.getLogger(ManagedFilesystemLockSupplier.class);

	private LockFileManager lockFilesMonitor = new LockFileManager();
	private long touchIntervalInMs = 10_000;
	private long touchWorkerIntervalInMs = 1_000;
	
	@Configurable
	public void setTouchIntervalInMs(long touchIntervalInMs) {
		this.touchIntervalInMs = touchIntervalInMs;
	}
	
	@Configurable
	public void setTouchWorkerIntervalInMs(long touchWorkerIntervalInMs) {
		this.touchWorkerIntervalInMs = touchWorkerIntervalInMs;
	}
	
	public List<Path> getCurrentlyManagedLockFiles() {
		List<Path> paths = new ArrayList<>();
		lockFilesMonitor.iterator().forEachRemaining(n -> paths.add(n.path));
		return paths;
	}
	
	private static class NodeIterator implements Iterator<ManagedLockFile> {
		private ManagedLockFile node;
		private ManagedLockFile to;
		
		public NodeIterator(ManagedLockFile from, ManagedLockFile to) {
			super();
			this.to = to;
			this.node = from;
		}

		@Override
		public boolean hasNext() {
			return node.next != to;
		}
		
		@Override
		public ManagedLockFile next() {
			return node = node.next;
		}
	}
	
	public static class ManagedLockFile {
		public Path path;
		public ManagedLockFile prev;
		public ManagedLockFile next;
		public long lastModified;
		
		public ManagedLockFile() {
			
		}
		
		public ManagedLockFile(boolean selfLinked) {
			if (selfLinked) {
				next = prev = this;
			}
		}
		
		public void insertBefore(ManagedLockFile node) {
			next = node;
			prev = node.prev;
			
			next.prev = this;
			prev.next = this;
		}
		
		public void remove() {
			ManagedLockFile p = prev;
			ManagedLockFile n = next;
			p.next = n;
			n.prev = p;
		}
		
		public void moveBefore(ManagedLockFile node) {
			if (this == node)
				return;
			
			// remove this node from it previous and next node and shortcut them
			remove();
			
			// link this node before node
			insertBefore(node);
		}
	}
			
	private class LockFileManager extends Thread implements ManagedLockFiles {
		
		private Map<Path, ManagedLockFile> nodeMap = new IdentityHashMap<>(); 
		public ManagedLockFile anchor = new ManagedLockFile(true);
		
		public LockFileManager() {
			setPriority(MAX_PRIORITY);
		}
		
		public Iterator<ManagedLockFile> iterator() {
			return new NodeIterator(anchor, anchor);
		}
		
		public Iterator<ManagedLockFile> iterator(ManagedLockFile node) {
			return new NodeIterator(node, anchor);
		}
		
		@Override
		public void remove(Path file) {
			synchronized (this) {
				ManagedLockFile node = nodeMap.remove(file);
					
				if (node != null) {
					node.remove();
				}
			}
		}
		
		@Override
		public void add(Path path) {
			synchronized (this) {
				ManagedLockFile node = new ManagedLockFile();
				node.path = path;
				node.lastModified = System.currentTimeMillis();
				node.insertBefore(anchor);
				
				nodeMap.put(path, node);
			}
		}
		
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(touchWorkerIntervalInMs);
					touchLockFiles();
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		
		private Path[] getLockFilesToBeTouched() {
			synchronized (this) {
				ManagedLockFile node = anchor.next;
				
				long time = System.currentTimeMillis();
				
				int count = 0;
				
				while (node != anchor) {
					long delta = time - node.lastModified;
					
					if (delta < touchIntervalInMs) {
						break;
					}
					
					count++;
					
					node = node.next;
				}
				
				Path[] paths = new Path[count];
				int index = 0;
				ManagedLockFile matchedNode = anchor.next;
				while (matchedNode != node) {
					matchedNode.lastModified = time;
					paths[index++] = matchedNode.path;
					matchedNode = matchedNode.next;
				}
				
				// move anchor before node 
				anchor.moveBefore(node);
				
				return paths;
			}
		}
		
		private void touchLockFiles() {
			Path paths[] = getLockFilesToBeTouched();
			
			FileTime touchTime = FileTime.fromMillis(System.currentTimeMillis());
			
			for (Path path: paths) {
				try {
					Files.setLastModifiedTime(path, touchTime);
				} catch (IOException e) {
					log.error("Error while touching lock file: " + path, e);
				}
			}
		}

		@Override
		public long lockTimeToLiveInMs() {
			return touchIntervalInMs * 2;
		}
	};
	
	public ManagedFilesystemLockSupplier() {
		lockFilesMonitor.start();
	}

	@Override
	public ReadWriteLock apply(File file) {
		return get(file);
	}
	
	public ReadWriteLock get(File file) {
		return new ReadWriteLock() {
			@Override
			public Lock writeLock() {
				return new ManagedFilesystemLock(lockFilesMonitor, file);
			}
			
			@Override
			public Lock readLock() {
				return new ManagedFilesystemLock(lockFilesMonitor, file);
			}
		};
	}
	
	@Override
	public void preDestroy() {
		lockFilesMonitor.interrupt();
		try {
			lockFilesMonitor.join();
		}
		catch (InterruptedException e) {
			// NOP
		}
	}
	
	
}
