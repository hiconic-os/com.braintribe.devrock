// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk.contract;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.braintribe.build.artifact.api.RangedArtifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.wire.api.space.WireSpace;

public interface FilterConfigurationContract extends WireSpace {
	Predicate<? super RangedArtifact> artifactFilter();
	Predicate<? super Solution> solutionFilter();
	Predicate<? super Dependency> dependencyFilter();
	Predicate<? super PartTuple> partFilter();
	Collection<PartTuple> partExpectation();
	default boolean filterSolutionBeforeVisit() { return false; }
	default BiPredicate<? super Solution, ? super Dependency> solutionDependencyFilter() { return (s,d) -> true; }
}
