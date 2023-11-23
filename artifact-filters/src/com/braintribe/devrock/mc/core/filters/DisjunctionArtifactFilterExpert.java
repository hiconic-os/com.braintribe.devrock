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
