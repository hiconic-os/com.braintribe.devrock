package com.braintribe.build.artifact.retrieval.multi.enriching.queued;

import java.util.function.Predicate;

import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;

/**
 * API of queuing solution enrichers
 * @author pit
 *
 */
public interface QueueingEnricher {
	
	/**
	 * enrich the solution with the default tuples - normally set to the enricher
	 * @param solution - 
	 */
	void enrich( Solution solution);
	
	/**
	 * enrich the solution with the tuples (or add that to the queue)
	 * @param solution - the {@link Solution}
	 * @param tuples - the {@link PartTuple}s
	 */
	void enrich( Solution solution, PartTuple ... tuples);
	
	/**
	 * enrich the solution filtering the found parts (or add that to the queue)
	 * @param solution
	 * @param predicate
	 */
	void enrich( Solution solution, Predicate<PartTuple> predicate);
	
	/**
	 * wait for all enrichments to complete and then shutdown 
	 */
	void finalizeEnrichment();
	
	/**
	 * shutdown the enricher 
	 */
	void stopEnrichment();
}
