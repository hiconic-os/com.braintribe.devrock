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
package com.braintribe.devrock.mc.core.download;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.devrock.mc.api.download.PartDownloadManager;
import com.braintribe.devrock.mc.api.download.PartDownloadScope;
import com.braintribe.devrock.mc.api.event.EventBroadcaster;
import com.braintribe.devrock.mc.api.event.EventBroadcasterAttribute;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactPartResolver;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloadEnqueued;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloadProcessed;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloadProcessing;
import com.braintribe.execution.SimpleThreadPoolBuilder;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.processing.async.api.Promise;
import com.braintribe.processing.async.impl.HubPromise;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * implementation of a fair download manager, using a 'round robin'scheme to ensure that no download is swamped by other 
 * download requests  
 * 
 * @author pit / dirk
 *
 */
public class BasicPartDownloadManager implements PartDownloadManager, LifecycleAware {
	
	private int poolSize = 5;
	private final LazyInitialized<Thread> pollerThread = new LazyInitialized<Thread>(this::startPollerThread, this::stopPollerThread);
	private final LazyInitialized<ExecutorService> executor = new LazyInitialized<ExecutorService>(this::buildExecutor, this::shutdownExecutor);
	private ArtifactPartResolver partResolver;
	
	@Configurable
	@Required
	public void setPartResolver(ArtifactPartResolver partResolver) {
		this.partResolver = partResolver;
	}
	
	@Configurable
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
	
	/**
	 * internal representation of a 'job'
	 * @author pit / dirk
	 *
	 */
	private static class DownloadJob {
		CompiledArtifactIdentification artifactIdentification;
		PartIdentification partIdentification;
		AttributeContext attributeContext = AttributeContexts.peek();
		
		HubPromise<Maybe<ArtifactDataResolution>> promise = new HubPromise<>();

		public DownloadJob(CompiledArtifactIdentification artifactIdentification, PartIdentification partIdentification) {
			super();
			this.artifactIdentification = artifactIdentification;
			this.partIdentification = partIdentification;
		}
	}
	
	/**
	 * internal representation of a member in the round robin
	 * @author pit / dirk
	 *
	 */
	private static class RobinCandidate {
		RobinCandidate next;
		RobinCandidate prev;
		
		public RobinCandidate(boolean anchor) {
			if (anchor) {
				next = this;
				prev = this;
			}
		}
		
		public PartDownloadScopeImpl getScope() {
			return null;
		}
		
		Queue<DownloadJob> getQueue() {
			return null;
		}
	}
	
	private final RobinCandidate anchorCandidate = new RobinCandidate(true);

	/**
	 * @param scope
	 */
	private void ensureCandidate(PartDownloadScopeImpl scope) {
		
		synchronized (anchorCandidate) {
			if (scope.prev != null)
				return;
			
			RobinCandidate prev = anchorCandidate.prev;
			RobinCandidate next = anchorCandidate;
			
			scope.prev = prev;
			scope.next = next;
			prev.next = scope;
			next.prev = scope;
			
			anchorCandidate.notify();
		}
	}
	
	/**
	 * @param candidate
	 */
	private void remove(RobinCandidate candidate) {
		synchronized (anchorCandidate) {
			if (!candidate.getQueue().isEmpty())
				return;
			
			RobinCandidate next = candidate.next;
			RobinCandidate prev = candidate.prev;
			
			next.prev = prev;
			prev.next = next;
			
			candidate.prev = null;
		}
	}
	
	@Override
	public PartDownloadScope openDownloadScope() {
		pollerThread.get();		
		return new PartDownloadScopeImpl();
	}
	
	/**
	 * internal implementation of the {@link PartDownloadScope}
	 * @author pit / dirk
	 *
	 */
	private class PartDownloadScopeImpl extends RobinCandidate implements PartDownloadScope {
		private final Queue<DownloadJob> queue = new LinkedBlockingQueue<>();
		
		public PartDownloadScopeImpl() {
			super(false);
		}
		
		@Override
		public Queue<DownloadJob> getQueue() {
			return queue;
		}
		
		@Override
		public PartDownloadScopeImpl getScope() {
			return this;
		}
		
		@Override
		public Promise<Maybe<ArtifactDataResolution>> download(CompiledArtifactIdentification identification, PartIdentification partIdentification) {
			DownloadJob job = new DownloadJob(identification, partIdentification);
			queue.offer(job);
			
			ensureCandidate(this);
			
			return job.promise;
		}
	}
	
	/**
	 * @return - the {@link Thread} handling the polling
	 */
	private Thread startPollerThread() {
		Thread pollerThread = new Thread(this::roundRob);
		pollerThread.start();
		return pollerThread;
	}
	
	/**
	 * @param thread - the {@link Thread} handling the polling
	 */
	private void stopPollerThread(Thread thread) {
		try {
			thread.interrupt();
			thread.join();
		} catch (InterruptedException e) {
			// noop
		}
	}
	
	/**
	 * @return - a fully qualified {@link ExecutorService}
	 */
	private ExecutorService buildExecutor() {
		/* construct a pool that takes the configured poolSize but blocks on submits/executions when the
		 * configured amount of threads is already in use. We use a combination of SynchronousQueue and
		 * a reject handler that puts on this queue instead of simply adding like the default in order to achieve blocking 
		 */
		return SimpleThreadPoolBuilder.newPool() //
				.poolSize(poolSize, poolSize) //
				// This was commented out when ThreadPoolBuilder was moved to platform-api and heavily simplified (thus also renamed)
				// IF this is really needed, we can figure it out later.
				//.threadNamePrefix("artifact-part-download-thread") //
				.workQueue(new SynchronousQueue<Runnable>())
				.rejectionHandler(BasicPartDownloadManager::handleReject)
				.build();
	}
	
	/**
	 * if a Runnable cannot be queued, this is call.. we just push it back? 
	 */
	private static void handleReject(Runnable runnable, ThreadPoolExecutor executor) {
		try {
			executor.getQueue().put(runnable);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private void shutdownExecutor(ExecutorService executor) {
		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// noop
			e.printStackTrace();
		}
	}

	/**
	 * the actual download balancer - 
	 */
	private void roundRob() {
		try {
			RobinCandidate candidate = anchorCandidate;
			
			while (true) {
				if (Thread.interrupted())
					break;

				synchronized (anchorCandidate) {
					// if the double linked list is empty we wait for notification on insert
					if (candidate.next == candidate) {
						anchorCandidate.wait();
					}
					candidate = candidate.next;
				}
				
				// if the double linked list is not empty but we reached its pivotal anchor 
				// we must skip that payload-free anchor to start again from the beginning in order to cycle 
				if (candidate == anchorCandidate)
					continue;

				DownloadJob job = candidate.getQueue().poll();
				
				if (job == null) {
					remove(candidate);
					continue;
				}
				
				EventBroadcaster eventBroadcaster = job.attributeContext.findAttribute(EventBroadcasterAttribute.class).orElse(EventBroadcaster.empty);
				

				OnPartDownloadEnqueued enqueuedEvent = OnPartDownloadEnqueued.T.create();
				
				CompiledPartIdentification part = CompiledPartIdentification.from(job.artifactIdentification, job.partIdentification);
				enqueuedEvent.setPart(part);
				
				eventBroadcaster.sendEvent(enqueuedEvent);

				
				// We need submit to block in order not to unfairly transfer prioritize eagerly known candidate queues
				// because candidates could occur any time but would then be processed only after the previous ones
				AttributeContexts.with(job.attributeContext).run(() -> {

					executor.get().submit(() -> {
						OnPartDownloadProcessing processingEvent = OnPartDownloadProcessing.T.create();
						processingEvent.setPart(part);
						
						eventBroadcaster.sendEvent(processingEvent);

						try {
							final Maybe<ArtifactDataResolution> maybe;
							try {
								maybe = partResolver.resolvePart(job.artifactIdentification, job.partIdentification);
							}
							finally {
								OnPartDownloadProcessed processedEvent = OnPartDownloadProcessed.T.create();
								processedEvent.setPart(part);
								
								eventBroadcaster.sendEvent(processedEvent);
							}
							job.promise.onSuccess(maybe);
						}
						catch (Throwable e) {
							job.promise.onFailure(e);
						}
						
					});
				});
			}
		}
		catch (InterruptedException e) {
			// noop
		}
	}

	@Override
	public void postConstruct() {
		 
	}
	
	@Override
	public void preDestroy() {
		pollerThread.close();
		executor.close();
	}
}
