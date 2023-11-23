// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.enriching;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.braintribe.build.artifact.retrieval.multi.cache.PartCacheAware;
import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationBroadcaster;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;

public interface MultiRepositorySolutionEnricher extends PartCacheAware, SolutionEnricherNotificationBroadcaster {
	
	// TODO: should be only known to the implementation and the instantiators
	/**
	 * set the currently relevant {@link PartTuple}
	 * @param tuples - {@link Collection} of {@link PartTuple}, in case you wondered 
	 */
	void setRelevantPartTuples( Collection<PartTuple> tuples);
	
	/**
	 * automatically enrich all solutions with the currently relevant part tuples
	 * @param walkScopeId - the id of the current walk
	 * @param solutions - a {@link Collection} of {@link Solution}
	 * @return - the enriched {@link Solution}
	 * @throws EnrichingException - if anything goes wrong catastrophically 
	 */
	Collection<Solution> enrich( String walkScopeId, Collection<Solution> solutions) throws EnrichingException;
	
	/**
	 * automatically enrich all solutions with the passed part tuple
	 * @param walkScopeId - the id of the current walk
	 * @param solutions - a {@link Collection} of {@link Solution}
	 * @param tuple - the {@link PartTuple} to enrich
	 * @return - the enriched {@link Solution}
	 * @throws EnrichingException - if anything goes wrong catastrophically 
	 */
	Collection<Solution> enrich( String walkScopeId, Collection<Solution> solutions, PartTuple tuple) throws EnrichingException;
	
	/**
	 * enrich a single part (given by the single {@link PartTuple}
	 * @param walkScopeId - the id of the current walk 
	 * @param solution - the {@link Solution} to enrich 
	 * @param tuple - the {@link PartTuple} that is relevant 
	 * @return - the enriched {@link Part} or null if it doesn't exist 
	 * @throws EnrichingException -
	 */
	Part enrich( String walkScopeId, Solution solution, PartTuple tuple) throws EnrichingException;
	
	/**
	 * enrich a single part (given by the single {@link PartTuple}
	 * @param walkScopeId - the id of the current walk 
	 * @param solution - the {@link Solution} to enrich 
	 * @param tuple - the {@link PartTuple} that is relevant 
	 * @return - a pair of the enriched {@link Part} and a boolean that reflects about a fresh download or null if it doesn't exist 
	 * @throws EnrichingException -
	 */
	Pair<Part, Boolean> enrichAndReflectDownload( String walkScopeId, Solution solution, PartTuple tuple) throws EnrichingException;
	
	/**
	 * enriches all passed parts of the solution
	 * @param walkScopeId
	 * @param solution
	 * @param tuples
	 * @return
	 * @throws EnrichingException
	 */
	default List<Pair<Part,Boolean>> enrichAndReflectDownload( String walkScopeId, Solution solution, List<PartTuple> tuples) throws EnrichingException {
		Pair<Part,Boolean> result = enrichAndReflectDownload(walkScopeId, solution, tuples.get(0));
		return Collections.singletonList( result);
	}
	
	// TODO: should be only known to the implementation and the instantiators
	void setAbortSignaller( ProcessAbortSignaller signaller);
}
