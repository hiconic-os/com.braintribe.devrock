// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.api;

import java.util.Set;

import com.braintribe.model.artifact.Solution;
import com.braintribe.utils.collection.api.MultiMap;

public interface BuildRangeDependencySolution {
	Set<Solution> getSolutions();
	MultiMap<Solution, Solution> getSolutionDependencyRelations(); 
}
