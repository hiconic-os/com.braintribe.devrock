// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.clash.merger;

import java.util.Collection;
import java.util.List;

import com.braintribe.build.artifact.walk.multi.clash.merger.listener.DependencyMergerNotificationBroadcaster;
import com.braintribe.model.artifact.Dependency;

/**
 * the API of the dependency merger - actually, the merger of version ranges, i.e. combines multiple ranges into a single one if possible 
 * @author pit
 *
 */
public interface DependencyMerger extends DependencyMergerNotificationBroadcaster {
	
	/**
	 * merge dependencies 
	 * @param walkScopeId - the id 
	 * @param dependencies - the {@link Collection} of {@link Dependency} to try to merge 
	 * @param droppedDependencies - the {@link Collection} of {@link Dependency} that can be dropped, i.e. forgotten about
	 * @return - the cleared {@link List} of {@link Dependency}
	 */
	List<Dependency> mergeDependencies( String walkScopeId, Collection<Dependency> dependencies, Collection<Dependency> droppedDependencies);	
}
