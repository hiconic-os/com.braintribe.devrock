// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.listener;

import java.util.List;

import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;

/**
 * listener for reports from the {@link Walker}
 * @author pit
 *
 */
public interface WalkNotificationListener {

	/**
	 * acknowledge walk start 
	 * @param solution - the terminal {@link Solution} on which the walk starts 
	 */
	void acknowledgeStartOn( String walkScopeId, Solution solution, WalkDenotationType denotationType);
	/**
	 * acknowledge walk end 
	 * @param solution - the terminal {@link Solution} on which the walk has ended
	 */
	void acknowledgeEndOn( String walkScopeId, Solution solution);
	
	/**
	 * acknowledge result of walk - the list of solutions 
	 * @param solutions - the {@link List} of {@link Solution} determined by the walk
	 */
	void acknowledgeWalkResult( String walkScopeId, List<Solution> solutions);
	
	/**
	 * acknowledge collected dependencies of walk 
	 * @param collectedDependencies - a {@link List} of all remaining {@link Dependency} after the walk 
	 */ 
	void acknowledgeCollectedDependencies( String walkScopeId, List<Dependency> collectedDependencies);
	
	void acknowledgeTraversingPhase(String walkScopeId);
	
	void acknowledgeDeterminationPhase(String walkScopeId,int numUndetermined);
	
	void acknowledgeDependencyClashResolvingPhase(String walkScopeId, int nunDependencies);
	
	void acknowledgeSolutionClashResolvingPhase(String walkScopeId, int numSolutions);
	
	void acknowledgeEnrichingPhase(String walkScopeId, int numSolutions);
	
	
	/**
	 * acknowledge that a clash exists between two dependencies (matching, or adhoc clashing), 
	 * so that the classifiers attached do not match.  
	 * @param dependency - the {@link Dependency} in question (the winner if cached or adhoc clashed)
	 * @param current - chosen classifiert (first come)
	 * @param requested - the mismatching classifier
	 */
	
	void acknowledgeClashOnDependencyClassifier( String walkScopeId, Dependency dependency, String current, String requested);
	/**
	 * acknowledge that this dependency cannot be resolved 
	 * @param dependency - the {@link Dependency} 
	 */
	void acknowledgeUnresolvedDependency( String walkScopeId, Dependency dependency);
	
	/**
	 * acknowledge that a undetermined dependency has been reassigned as a match has been found  
	 * @param undetermined - the undetermined {@link Dependency} (i.e. version's missing) 
	 * @param replacement - the matching {@link Dependency} it has been replaced with
	 */
	void acknowledgeReassignedDependency( String walkScopeId, Dependency undetermined, Dependency replacement);
	
	/**
	 * acknowledge that a dependency remains undetermined (no version found for it) 
	 * @param undetermined - the {@link Dependency} 
	 */
	void acknowledgeUndeterminedDependency( String walkScopeId, Dependency undetermined);
	
	/**
	 * acknowledge that a {@link Solution} is examined 
	 * @param artifact - the {@link Solution}
	 * @param level - the hierarchy level
	 * @param valid - whether the pom's valid
	 */
	void acknowledgeTraversing( String walkScopeId, Solution artifact, Dependency parent, int level, boolean valid);
	
	void acknowledgeTraversing( String walkScopeId, Dependency dependency, Solution parent, int level);
		
	
	/**
	 * acknowledge that a {@link Dependency} is not examined as it's been cached (i.e. points to a dependency that has been traversed already)  
	 * @param dependency - the {@link Dependency}
	 * @param level - the hierarchy level
	 */
	void acknowledgeTraversingEndpoint( String walkScopeId, Dependency dependency, Solution parent, int level);
	
	/**
	 * acknowledge that a {@link Solution} is not examined as it's been processed already (i.e. cached)
	 * @param solution - the solution found in the cache
	 * @param level - the hierarchy level
	 */
	void acknowledgeTraversingEndpoint( String walkScopeId, Solution solution, Dependency parent, int level);
}
