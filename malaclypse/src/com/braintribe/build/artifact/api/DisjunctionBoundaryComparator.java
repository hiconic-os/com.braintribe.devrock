// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DisjunctionBoundaryComparator implements Function<RangedArtifact, BoundaryHit> {

	private List<Function<RangedArtifact, BoundaryHit>> operands = new ArrayList<>();
	
	public void addOperand(Function<RangedArtifact, BoundaryHit> operand) {
		this.operands.add(operand);
	}

	public boolean isEmpty() {
		return operands.isEmpty();
	}
	
	@Override
	public BoundaryHit apply(RangedArtifact t) {
		for (Function<RangedArtifact, BoundaryHit> operand: operands) {
			BoundaryHit hit = operand.apply(t);
			if (hit != BoundaryHit.none)
				return hit;
		}
		return BoundaryHit.none;
	}

}
