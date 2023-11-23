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

import com.braintribe.devrock.model.repository.filters.StandardDevelopmentViewArtifactFilter;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * Expert implementation for {@link StandardDevelopmentViewArtifactFilter}.
 *
 * @author michael.lafite
 */
public class StandardDevelopmentViewArtifactFilterExpert implements ArtifactFilterExpert {

	private ArtifactFilterExpert restrictionFilter;
	boolean restrictOnArtifactInsteadOfGroupLevel;

	public StandardDevelopmentViewArtifactFilterExpert(ArtifactFilterExpert restrictionFilter, boolean restrictOnArtifactInsteadOfGroupLevel) {
		this.restrictionFilter = restrictionFilter;
		this.restrictOnArtifactInsteadOfGroupLevel = restrictOnArtifactInsteadOfGroupLevel;
	}

	private boolean isRestrictionFilterResponsible(ArtifactIdentification identification) {
		boolean result;
		if (restrictOnArtifactInsteadOfGroupLevel) {
			// filter is responsible only if it matches the artifact
			result = restrictionFilter.matches(identification);
		} else {
			// filter is responsible if it matches the group, i.e. any artifact of that group
			result = restrictionFilter.matchesGroup(identification.getGroupId());
		}
		return result;
	}

	@Override
	public boolean matchesGroup(String groupId) {
		// no need to check whether the restriction filter is responsible, because if it is, it returns true and otherwise we also return true
		return true;
	}

	@Override
	public boolean matches(ArtifactIdentification identification) {
		return isRestrictionFilterResponsible(identification) ? restrictionFilter.matches(identification) : true;
	}

	@Override
	public boolean matches(CompiledArtifactIdentification identification) {
		return isRestrictionFilterResponsible(identification) ? restrictionFilter.matches(identification) : true;
	}

	@Override
	public boolean matches(CompiledPartIdentification identification) {
		return isRestrictionFilterResponsible(identification) ? restrictionFilter.matches(identification) : true;
	}
}
