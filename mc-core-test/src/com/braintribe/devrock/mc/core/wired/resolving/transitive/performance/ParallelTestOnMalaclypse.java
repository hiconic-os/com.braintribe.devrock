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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.execution.SimpleThreadPoolBuilder;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.ClashResolvingStrategy;
import com.braintribe.utils.lcd.LazyInitialized;

public class ParallelTestOnMalaclypse extends AbstractClasspathResolvingPerformanceTest {
	private final int poolSize = 10;
	private final LazyInitialized<ExecutorService> executor = new LazyInitialized<ExecutorService>(this::buildExecutor, this::shutdownExecutor);	
	
	/**
	 * @return - a fully qualified {@link ExecutorService}
	 */
	private ExecutorService buildExecutor() {
		/* construct a pool that takes the configured poolSize but blocks on submits/executions when the
		 * configured amount of threads is already in use. We use a combination of SynchronousQueue and
		 * a reject handler that puts on this queue instead of simply adding like the default in order to achieve blocking 
		 */
		return SimpleThreadPoolBuilder
				.newPool() //
				.poolSize(poolSize, poolSize) //
				// Commented out when ThreadPoolBuilder was moved to platform-api and heavily simplified (thus also renamed)
				//.threadNamePrefix("mc-ng-thread") //
				.workQueue(new SynchronousQueue<Runnable>())
				.rejectionHandler(ParallelTestOnMalaclypse::handleReject)
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
	
	private ClasspathResolutionContext resolutionContext() {
		return ClasspathResolutionContext.build().clashResolvingStrategy(ClashResolvingStrategy.highestVersion).enrichJavadoc(true).enrichSources(true).done();
	}

	
	private String generateTag( boolean prime, boolean reinitialize) {
		if (reinitialize) {
			return "with filesystem cache";
		}
		else if (prime) {
			return "with full memory cache";
		}
		else {
			return "without cache";
		}
	}
	
	
	
	private double parallelRunTest(int maxRuns, boolean prime, boolean reinitialize) {
		
		TestUtils.ensure(repo);
		reinitialize();
		TestUtils.initializeLogging( input);
		
		// prime the cache
		if (prime) {
			Pair<AnalysisArtifactResolution, Long> primeResult = resolve("com.braintribe.devrock:malaclypse#[1.0,1.1)", resolutionContext());
			System.out.println("primed in [" + primeResult.second / 1E6 + "] ms");
			if (reinitialize) {
				reinitialize();
				System.out.println("reinitialized");
			}
		}		
		
		
		long before = System.nanoTime();
		List<Future<Pair<AnalysisArtifactResolution, Long>>> futures = new ArrayList<>();
		for (int i = 0; i < maxRuns; i++) {			
			Future<Pair<AnalysisArtifactResolution, Long>> future = executor.get().submit(() -> resolve("com.braintribe.devrock:malaclypse#[1.0,1.1)", resolutionContext()));
			futures.add(future);
		}
		
		
		double sum = 0;
		for (Future<Pair<AnalysisArtifactResolution, Long>> future : futures) {
			try {
				Pair<AnalysisArtifactResolution, Long> result = future.get();
				double d = result.second / 1E6;
				sum += d;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		long after = System.nanoTime();
		
		double dif = (after - before) / 1E6;
		System.out.println("loop took " + dif);
		return sum / futures.size();
	}
	
	
	protected void runNoCacheTest(int runs) {
		double d = parallelRunTest( runs, false, false);
		System.out.println("average for  [" + runs + "] " + generateTag(false, false) + " took : " + d + " ms");
	}
	
	
	protected void runFilesystemCacheTest(int runs) {	
		double d = parallelRunTest( runs, true, true);
		System.out.println("average for  [" + runs + "] " + generateTag( true, true) + " took : " + d + " ms");
	}
	
	protected void runFullCacheTest(int runs) {
		double d = parallelRunTest( runs, true, false);
		System.out.println("average for  [" + runs + "] " + generateTag( true, false) + " took : " + d + " ms");
	}
	
	//@Test
	public void runNumberedRuns() {
		int runs = 2;		
		runNoCacheTest(runs);
		runFilesystemCacheTest(runs);
		runFullCacheTest(runs);
	}
	
	//@Test
	public void runSequence() {
		int maxRuns = 10;
		
		for ( int i = 1; i <= maxRuns; i++) {
			runNoCacheTest( i);
			runFilesystemCacheTest( i);
			runFullCacheTest( i);
		}
	}
	
	
	
}
