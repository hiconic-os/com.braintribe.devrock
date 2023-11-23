// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.watcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.listener.RavenhurstNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemLock;
import com.braintribe.logging.Logger;

public class FileWatcher extends Thread {
	private static Logger log = Logger.getLogger(FileWatcher.class);
	private File directoryToWatch;
	private RavenhurstNotificationListener listener;
	private String repositoryUrl;

	public FileWatcher(File directoryToWatch, String repositoryUrl, RavenhurstNotificationListener listener) {
		this.directoryToWatch = directoryToWatch;
		this.repositoryUrl = repositoryUrl;
		this.listener = listener;
	}

	@Override
	public void run() {
		Path watchFile = Paths.get( directoryToWatch.toURI());
		WatchService watchService = null;
		try {
			 watchService = watchFile.getFileSystem().newWatchService();
			 watchFile.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
			
		} catch (IOException e) {
			log.error( "cannot start watcher on directory [" + directoryToWatch.getAbsolutePath() + "] as " + e, e);
		}
		for (;;) {
			try {
				WatchKey watchKey = watchService.take();
				for (WatchEvent<?> event : watchKey.pollEvents()) {
					if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY || event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
						Path path = (Path) event.context();
						String name = path.getFileName().toString();
						// do not notify a lock file nor an .index file 
						if (
								!name.endsWith( FilesystemLock.FILESYSTEM_LOCK_SUFFIX) &&
								!name.equalsIgnoreCase(".index")
							) {
							Thread.sleep(1000);
							listener.acknowledgeInterrogation(repositoryUrl);
						}
					}
				}
				watchKey.reset();
			} catch (InterruptedException e) {
				// shutdown					
				break;
			}				
		}
		// stop watching
		try {
			watchService.close();
		} catch (IOException e) {
			log.error( "cannot close watcher on directory [" + directoryToWatch + "] as " + e, e);
		}
	}
	
	
}
