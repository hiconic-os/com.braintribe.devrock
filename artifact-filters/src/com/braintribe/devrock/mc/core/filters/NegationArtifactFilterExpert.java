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

import com.braintribe.devrock.model.repository.filters.NegationArtifactFilter;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * Expert implementation for {@link NegationArtifactFilter}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class NegationArtifactFilterExpert implements ArtifactFilterExpert {

	private ArtifactFilterExpert operand;

	public NegationArtifactFilterExpert(ArtifactFilterExpert operand) {
		this.operand = operand;
	}

	@Override
	public boolean matchesGroup(String groupId) {
		return !operand.matchesGroup(groupId);
	}	
	
	@Override
	public boolean matches(ArtifactIdentification identification) {
		return !operand.matches(identification);
	}

	@Override
	public boolean matches(CompiledArtifactIdentification identification) {
		return !operand.matches(identification);
	}

	@Override
	public boolean matches(CompiledPartIdentification identification) {
		return !operand.matches(identification);
	}
}
