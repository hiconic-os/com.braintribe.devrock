// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
