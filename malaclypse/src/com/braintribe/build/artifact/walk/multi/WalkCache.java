// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi;

import java.util.Set;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

/**
 * the cache for the walk
 * @author Pit
 *
 */
public interface WalkCache {

	void addUnDeterminedDependency( Dependency dependency);
	Set<Dependency> getUnDeterminedDependencies( Dependency dependency);
	Set<Dependency> getCollectedUnDeterminedDependencies();
	
	void addDependency(Dependency dependency);	
	Dependency containsDependency( Dependency depenency);
	Set<Dependency> getCollectedDependencies();

	void addSolution( Solution solution);
	Solution containsSolution( Solution solution);
	Set<Solution> getCollectedSolutions();		
	
}
