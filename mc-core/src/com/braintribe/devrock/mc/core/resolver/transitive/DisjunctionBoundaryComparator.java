// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.devrock.mc.core.resolver.transitive;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.braintribe.devrock.mc.api.transitive.BoundaryHit;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

public class DisjunctionBoundaryComparator implements Function<CompiledArtifactIdentification, BoundaryHit> {

	private List<Function<CompiledArtifactIdentification, BoundaryHit>> operands = new ArrayList<>();
	
	public void addOperand(Function<CompiledArtifactIdentification, BoundaryHit> operand) {
		this.operands.add(operand);
	}

	public boolean isEmpty() {
		return operands.isEmpty();
	}
	
	@Override
	public BoundaryHit apply(CompiledArtifactIdentification t) {
		for (Function<CompiledArtifactIdentification, BoundaryHit> operand: operands) {
			BoundaryHit hit = operand.apply(t);
			if (hit != BoundaryHit.none)
				return hit;
		}
		return BoundaryHit.none;
	}

}
