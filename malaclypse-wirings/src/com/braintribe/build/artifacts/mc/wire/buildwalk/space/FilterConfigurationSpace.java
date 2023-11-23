// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk.space;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import com.braintribe.build.artifact.api.RangedArtifact;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.FilterConfigurationContract;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class FilterConfigurationSpace implements FilterConfigurationContract {
	
	private static Predicate<Object> truePredicate = o -> true;
	
	@Override
	public Predicate<? super RangedArtifact> artifactFilter() {
		return truePredicate;
	}
	
	@Override
	public Predicate<? super Solution> solutionFilter() {
		return truePredicate;
	}
	
	@Override
	public Predicate<? super Dependency> dependencyFilter() {
		return truePredicate;
	}

	@Override
	public Predicate<? super PartTuple> partFilter() {
		return null;
	}
	
	@Override
	public Collection<PartTuple> partExpectation() {
		return Collections.emptyList();
	}
	
	@Override
	public boolean filterSolutionBeforeVisit() {
		return false;
	}
}
