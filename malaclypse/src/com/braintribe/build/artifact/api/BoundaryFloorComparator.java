// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.api;

import java.util.function.Function;

public class BoundaryFloorComparator implements Function<RangedArtifact, BoundaryHit> {
	
	public static final BoundaryFloorComparator INSTANCE = new BoundaryFloorComparator();
	
	@Override
	public BoundaryHit apply(RangedArtifact t) {
		return t == RangedArtifacts.boundaryFloor()? BoundaryHit.open: BoundaryHit.none;
	}

}
