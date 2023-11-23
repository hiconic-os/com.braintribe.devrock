// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.api;

import java.util.function.Function;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;

public class BoundaryComparator implements Function<RangedArtifact, BoundaryHit> {
	
	private Dependency dependency;
	private boolean open;
	
	public BoundaryComparator(Dependency dependency, boolean open) {
		super();
		this.dependency = dependency;
		this.open = open;
	}

	@Override
	public BoundaryHit apply(RangedArtifact t) {
		if (
				dependency.getGroupId().equals(t.getGroupId()) && 
				dependency.getArtifactId().equals(t.getArtifactId()) && 
				VersionRangeProcessor.contains(dependency.getVersionRange(), t.getVersionRange())
			) {
			return open? BoundaryHit.open: BoundaryHit.closed;
		}
		return BoundaryHit.none;
	}

}
