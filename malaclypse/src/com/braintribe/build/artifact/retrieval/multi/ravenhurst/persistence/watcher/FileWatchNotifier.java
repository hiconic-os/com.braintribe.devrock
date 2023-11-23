// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.watcher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.listener.RavenhurstNotificationBroadcaster;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.listener.RavenhurstNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry.RavenhurstPersistenceHelper;
import com.braintribe.utils.paths.PathList;

public class FileWatchNotifier implements RavenhurstNotificationBroadcaster{
	
	private String localRepository;
	private Map<String, List<RavenhurstNotificationListener>> fileToListenerMap = new HashMap<String, List<RavenhurstNotificationListener>>();
	private Map<String, FileWatcher> fileToWatcherMap = new HashMap<String, FileWatcher>();
	
	public FileWatchNotifier(String localRepository) {
		this.localRepository = localRepository;
	}

	public static File buildFileNameForBundleDump(String localRepository, String repositoryId) {
		File bundleDump = PathList.create().push(localRepository).push( RavenhurstPersistenceHelper.UPDATE_DATA_STORAGE).push( repositoryId).toFile();
		return bundleDump;
	}
	
		@Override
	public void addRavenhurstNotificationListener(String repositoryId, RavenhurstNotificationListener listener) {
		File fileToWatch = 	buildFileNameForBundleDump(localRepository, repositoryId);
		List<RavenhurstNotificationListener> listeners = fileToListenerMap.get(fileToWatch.getAbsolutePath());
		if (listeners == null) {
			listeners = new ArrayList<RavenhurstNotificationListener>();
			fileToListenerMap.put(fileToWatch.getAbsolutePath(), listeners);			
		}
		listeners.add(listener);
		// build file watcher 
		FileWatcher watcher = new FileWatcher(fileToWatch, repositoryId, listener);
		fileToWatcherMap.put( repositoryId, watcher);
		watcher.start();
	}

	@Override
	public void removeRavenhurstNotificationListener(String repositoryId, RavenhurstNotificationListener listener) {
		File fileToWatch = 	buildFileNameForBundleDump(localRepository, repositoryId);
		List<RavenhurstNotificationListener> listeners = fileToListenerMap.get(fileToWatch.getAbsolutePath());
		if (listeners == null) {
			return;
		}
		listeners.remove(listener);
		if (!listeners.isEmpty()) {
			return; 
		}
		// remove file watcher
		FileWatcher watcher = fileToWatcherMap.get( repositoryId);
		if (watcher != null) {
			watcher.interrupt();
			try {
				watcher.join();
			} catch (InterruptedException e) {
			}
		}		
	}
	
	public void removeAll() {
		for (FileWatcher watcher : fileToWatcherMap.values()) {
			watcher.interrupt();
			try {
				watcher.join();
			} 
			catch (InterruptedException e) {			
			}
		}
		fileToListenerMap.clear();
		fileToWatcherMap.clear();
	}
	
	

}
