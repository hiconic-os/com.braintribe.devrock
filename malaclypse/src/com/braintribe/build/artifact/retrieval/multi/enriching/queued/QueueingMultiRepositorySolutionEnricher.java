package com.braintribe.build.artifact.retrieval.multi.enriching.queued;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationBroadcaster;
import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;

/**
 * future/executor based: add a solution to the queue and it will enrich that 
 * 
 * @author pit
 *
 */
public class QueueingMultiRepositorySolutionEnricher implements ConfigurableQueueingEnricher, SolutionEnricherNotificationListener, SolutionEnricherNotificationBroadcaster, ProcessAbortSignaller {
	private static Logger log = Logger.getLogger(QueueingMultiRepositorySolutionEnricher.class);
	
	private List<SolutionEnricherNotificationListener> listeners = new ArrayList<>();
	private ProcessAbortSignaller abortSignaller;	
	private RepositoryReflection repositoryReflection;
	private boolean disableDependencyPartExpectationClassifierInfluence = false;	
	private Set<Solution> processed = new HashSet<>();
	@SuppressWarnings("unused")
	private LockFactory lockFactory;
	
	private ExecutorService executorService = Executors.newFixedThreadPool(4);	

	private List<Future<EnrichingPacket>> futures = new ArrayList<>(); 
	
	private List<Solution> runningEnrichments = new ArrayList<>();

	private boolean myAbortSignal = false;
	private boolean initialized = false;
	
	private PartTuple [] relevantPartTuples;
	private Predicate<PartTuple> predicate;

	/**	
	 * @param repositoryRegistry - the configured {@link RepositoryReflection}
	 */
	@Configurable @Required
	public void setRepositoryReflection(RepositoryReflection repositoryRegistry) {
		this.repositoryReflection = repositoryRegistry;
	}
	
	/**
	 * @param disableDependencyPartExpectationClassifierInfluence - true if part names are influenced the dependency's classifier
	 */
	@Configurable
	public void setDisableDependencyPartExpectationClassifierInfluence(boolean disableDependencyPartExpectationClassifierInfluence) {
		this.disableDependencyPartExpectationClassifierInfluence = disableDependencyPartExpectationClassifierInfluence;
	}	
	
	/**
	 * @param signaller - signaller that polled for a abort signal
	 */
	@Configurable
	public void setAbortSignaller(ProcessAbortSignaller signaller) {
		abortSignaller = signaller;
	}
	
	@Configurable
	public void setRelevantPartTuples(PartTuple ... relevantPartTuples) {
		this.relevantPartTuples = relevantPartTuples;
	}
	
	@Configurable
	public void setRelevantPartPredicate(Predicate<PartTuple> predicate) {
		this.predicate = predicate;
	}
	
	@Override @Configurable
	public void setLockFactory(LockFactory lockFactory) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * clear the {@link Set} that stores all successfully enriched solutions
	 */
	public void clearCache() {
		processed.clear();
	}
	
	
	@Override
	public boolean abortScan() {
		// if abort signaller is set, and we're not aborting, take its value
		if (abortSignaller != null && !myAbortSignal) {
			myAbortSignal = abortSignaller.abortScan();			
		}
		return myAbortSignal;
	}

	@Override
	public void addListener(SolutionEnricherNotificationListener listener) {
		synchronized( listeners) {
			listeners.add(listener);
		}
	}
	
	@Override
	public void removeListener(SolutionEnricherNotificationListener listener) {
		synchronized( listeners) {
			listeners.remove(listener);
		}
	}
	
	

	@Override
	public void enrich(Solution solution) {
		if (relevantPartTuples == null && predicate == null) {
			throw new IllegalStateException("neither relevant part tuples nor predicate set");
		}
		if (relevantPartTuples != null) {
			enrich( solution, relevantPartTuples);
		}
		else if (predicate != null) {
			enrich(solution,  predicate);
		}		
	}

	@Override
	public void enrich(Solution solution, PartTuple... tuples) {				
		initialize();
		if (processed.contains(solution) || runningEnrichments.contains(solution)) {
			return;
		}
		
		EnrichingPacket packet = new EnrichingPacket();
		packet.solution = solution;
		packet.tuples = tuples;
		packet.listener = this;
		packet.abortSignaller = this;
		
		Worker worker = new Worker( packet);				
		runningEnrichments.add( packet.solution);				
		Future<EnrichingPacket> workerFuture = executorService.submit(worker);	
		futures.add( workerFuture);
		
	}
	@Override
	public void enrich(Solution solution, Predicate<PartTuple> predicate) {
		initialize();
		if (processed.contains(solution) || runningEnrichments.contains(solution)) {
			return;
		}
		
		EnrichingPacket packet = new EnrichingPacket();
		packet.solution = solution;
		packet.predicate = predicate;
		packet.listener = this;
		packet.abortSignaller = this;
		
		Worker worker = new Worker( packet);				
		runningEnrichments.add( packet.solution);				
		Future<EnrichingPacket> workerFuture = executorService.submit(worker);	
		futures.add( workerFuture);
	}	
	
	private String toString( PartTuple [] tuples) {
		return Arrays.asList( tuples).stream().map( t -> {
			return PartTupleProcessor.toString(t);
		}).collect( Collectors.joining(","));
	}
	
	

	@Override
	public void finalizeEnrichment() {
				
		futures.stream().forEach( f -> {
			try {
				EnrichingPacket packet = f.get();
				if (log.isDebugEnabled()) {
					log.debug( "retrieved future for [" + NameParser.buildName(packet.solution) + "] and [" + toString(packet.tuples) + "]"); 
				}
			} catch (Exception e) {			
				e.printStackTrace();
			}
		});
		
			
		stopEnrichment();
	}
	
		
	@Override
	public void stopEnrichment() {
		// set the current abort signal, to quickly terminate running callers 
		myAbortSignal = true;
		if (!initialized)
			return;		
		executorService.shutdown();
		
	}	
	

	private void initialize() {
		if (initialized)
			return;
		initialized = true;
	}


	@Override
	public void acknowledgeFileEnrichmentSuccess(String walkScopeId, String file) {
		synchronized( listeners) {
			listeners.stream().forEach( l -> l.acknowledgeFileEnrichmentSuccess(walkScopeId, file));
		}
	}

	@Override
	public void acknowledgeFileEnrichmentFailure(String walkScopeId, Solution solution, PartTuple tuple) {
		synchronized( listeners) { 
			listeners.stream().forEach( l -> l.acknowledgeFileEnrichmentFailure(walkScopeId, solution, tuple));
		}		
	}

	@Override
	public void acknowledgeSolutionEnriching(String walkScopeId, Solution solution) {
		synchronized (processed) {
			processed.add(solution);
		}
		synchronized( listeners) {
			listeners.stream().forEach( l -> l.acknowledgeSolutionEnriching(walkScopeId, solution));
		}
	}
			
	// 
	// runnables 
	//
	/**
	 * the caller that actually processes a taken {@link EnrichingPacket}
	 * 
	 * @author pit
	 *
	 */
	private class Worker implements Callable<EnrichingPacket> {
		private EnrichingPacket packet;
		public Worker( EnrichingPacket packet){
			this.packet = packet;
		}				
	
		@Override
		public EnrichingPacket call() {	
			TransactionalEnricher enricher = new TransactionalEnricher();
			enricher.setAbortSignaller( packet.abortSignaller);
			enricher.setRepositoryReflection( repositoryReflection);
			enricher.setDisableDependencyPartExpectationClassifierInfluence(disableDependencyPartExpectationClassifierInfluence);
			enricher.setListener( packet.listener);			
			try {
				if (packet.predicate != null) {
					enricher.enrich( packet.walkScopeId, packet.solution, packet.predicate);
				}
				else {
					enricher.enrich( packet.walkScopeId, packet.solution, packet.tuples);
				}

			}
			finally {				
				synchronized (runningEnrichments) {
					runningEnrichments.remove(packet.solution);
				}
			}
			return packet;
		}	
	}	
}
