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
