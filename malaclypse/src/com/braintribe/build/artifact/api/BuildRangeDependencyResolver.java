// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.api;

import java.util.Set;
import java.util.function.Function;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

public interface BuildRangeDependencyResolver extends DependencyResolver {
	Set<Solution> resolve(Iterable<Dependency> dependencies, Function<RangedArtifact, BoundaryHit> lowerBoundary, Function<RangedArtifact, BoundaryHit> upperBoundary);
	BuildRangeDependencySolution resolve(BuildRange config);
}
