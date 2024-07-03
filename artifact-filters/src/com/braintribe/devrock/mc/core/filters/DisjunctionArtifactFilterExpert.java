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
package com.braintribe.devrock.mc.core.filters;

import java.util.List;

import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * Expert implementation for {@link DisjunctionArtifactFilter}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class DisjunctionArtifactFilterExpert implements ArtifactFilterExpert {

	private List<ArtifactFilterExpert> operands;

	public DisjunctionArtifactFilterExpert(List<ArtifactFilterExpert> operands) {
		this.operands = operands;
	}

	@Override
	public boolean matchesGroup(String groupId) {
		boolean result = false;
		for (ArtifactFilterExpert operand : operands) {
			if (operand.matchesGroup(groupId)) {
				result = true;
				break;
			}
		}
		return result;
	}	
	
	@Override
	public boolean matches(ArtifactIdentification identification) {
		boolean result = false;
		for (ArtifactFilterExpert operand : operands) {
			if (operand.matches(identification)) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public boolean matches(CompiledArtifactIdentification identification) {
		boolean result = false;
		for (ArtifactFilterExpert operand : operands) {
			if (operand.matches(identification)) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public boolean matches(CompiledPartIdentification identification) {
		boolean result = false;
		for (ArtifactFilterExpert operand : operands) {
			if (operand.matches(identification)) {
				result = true;
				break;
			}
		}
		return result;
	}
}
