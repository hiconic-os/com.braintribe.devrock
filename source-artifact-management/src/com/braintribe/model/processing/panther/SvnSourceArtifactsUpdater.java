// ============================================================================
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
package com.braintribe.model.processing.panther;

import java.util.function.Supplier;

import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class SvnSourceArtifactsUpdater implements SourceArtifactsUpdater, LifecycleAware {
	private static final Logger logger = Logger.getLogger(SvnSourceArtifactsUpdater.class);
	private static enum ScanJob {
		none, global, incremental
	}
	
	private ScanJob scanJob = ScanJob.none;
	private String scanLock = "scanLock";
	private Worker worker = new Worker();
	private Supplier<PersistenceGmSession> sessionProvider;
	
	@Required
	public void setSessionProvider(Supplier<PersistenceGmSession> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}
	
	@Override
	public void scanGlobal() {
		synchronized (scanLock) {
			if (scanJob != ScanJob.global) {
				scanJob = ScanJob.global;
				scanLock.notify();
			}
		}
	}
	
	@Override
	public void scanIncremental() {
		synchronized (scanLock) {
			if (scanJob == ScanJob.none) {
				scanJob = ScanJob.incremental;
				scanLock.notify();
			}
		}
	}
	
	@Override
	public String test(String text) {
		return text.toUpperCase();
	}
	
	@Override
	public void postConstruct() {
		worker.start();
	}

	@Override
	public void preDestroy() {
		worker.interrupt();
		try {
			worker.join();
		} catch (InterruptedException e) {
			logger.info("Got interrupted while waiting for worker to finish.", e);
		}
	}
	
	private class Worker extends Thread {
		@Override
		public void run() {
			while (true) {

				scanLoop: while (true) {
					switch (takeJob()) {
					case global:
						_scanGlobal();
						break;
					case incremental:
						_scanIncremental();
						break;
					case none:
						break scanLoop;
					}
				}

				synchronized (scanLock) {
					try {
						scanLock.wait();
					} catch (InterruptedException e) {
						return;
					}

				}
			}
		}

		private void _scanIncremental() {
			try {
				Sam sam = new Sam();
				sam.setSessionProvider(sessionProvider);
				sam.incrementalUpdate();
			} catch (Exception e) {
				logger.error("error while doing incremental update");
			}
		}

		private void _scanGlobal() {
			try {
				Sam sam = new Sam();
				sam.setSessionProvider(sessionProvider);
				
				sam.globalUpdate();
			} catch (Exception e) {
				logger.error("error while doing global update");
			}
		}
	}
	
	protected ScanJob takeJob() {
		synchronized (scanLock) {
			ScanJob job = scanJob;
			scanJob = ScanJob.none;
			return job;
		}
	}

}
