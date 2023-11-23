// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.api;

import java.util.List;
import java.util.function.Function;

import com.braintribe.model.artifact.Dependency;

public class BuildRange {
	private List<Dependency> entryPoints;
	private Function<RangedArtifact, BoundaryHit> lowerBound;
	private Function<RangedArtifact, BoundaryHit> upperBound;
	
	public BuildRange(List<Dependency> entryPoints, Function<RangedArtifact, BoundaryHit> lowerBound,
			Function<RangedArtifact, BoundaryHit> upperBound) {
		super();
		this.entryPoints = entryPoints;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public List<Dependency> getEntryPoints() {
		return entryPoints;
	}
	
	public Function<RangedArtifact, BoundaryHit> getLowerBound() {
		return lowerBound;
	}
	
	public Function<RangedArtifact, BoundaryHit> getUpperBound() {
		return upperBound;
	}
}
