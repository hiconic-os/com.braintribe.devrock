// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.artifact.walk.multi.clash;

import java.util.Collection;
import java.util.List;

import com.braintribe.build.artifact.walk.multi.clash.listener.ClashResolverNotificationBroadcaster;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

/**
 * the common API all clash resolvers must implement
 * @author pit
 *
 */
public interface ClashResolver extends ClashResolverNotificationBroadcaster {
	/**
	 * resolve all clashes within the dependencies passed and return a cleared list, using the logic of the actual 
	 * implementation 
	 * @param dependencies - a {@link Collection} of {@link Dependency} to act upon, most times the collected dependencies of a walk
	 * @return - a {@link List} of cleared dependencies - without any clashes 
	 */
	List<Dependency> resolveDependencyClashes( String walkScopeId, Solution terminal, Collection<Dependency> dependencies) throws ClashResolvingException;
	
	/**
	 * resolve all clashes within the solutions, and return a cleared list  
	 * @param solution - a {@link Collection} of {@link Solution} to act upon 
	 * @return - 
	 */
	List<Solution> resolveSolutionClashes( String walkScopeId, Collection<Solution> solution) throws ClashResolvingException;
	
	
	
	/**
	 * returns true if this {@link Dependency} is already determined and doesn't need to be processed any further.
	 * Only the index based resolver may return true, all others return false (they're posthoc clashers)
	 * @param dependency - the {@link Dependency} to test
	 * @return - true if it's been declared already, false otherwise 
	 */
	Dependency adhocClashResolving( String walkScopeId, Dependency dependency) throws ClashResolvingException;
}
