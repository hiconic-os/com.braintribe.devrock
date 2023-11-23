// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.enriching.listener;

import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;

/**
 * @author pit
 *
 */
public interface SolutionEnricherNotificationListener {

	/**
	 * enricher was able to download a file 
	 * @param walkScopeId -
	 * @param file - the full path of the (local) file 
	 */
	void acknowledgeFileEnrichmentSuccess( String walkScopeId, String file);
	/**
	 * enricher wasn't able to download a file
	 * @param walkScopeId - 
	 * @param solution - {@link Solution} where it failed 
	 * @param tuple - the tuple that failed
	 */
	void acknowledgeFileEnrichmentFailure (String walkScopeId, Solution solution, PartTuple tuple);
	
	/**
	 * enricher starts enriching on the solution 
	 * @param walkScopeId -  
	 * @param solution - the {@link Solution} to be enriched 
	 */
	void acknowledgeSolutionEnriching( String walkScopeId, Solution solution);
}
