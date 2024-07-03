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

import java.util.Set;

import com.braintribe.devrock.model.repository.filters.GroupsArtifactFilter;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * Expert implementation for {@link GroupsArtifactFilter}.
 *
 * @author michael.lafite
 */
public class GroupsArtifactFilterExpert implements ArtifactFilterExpert {

	private Set<String> groups;

	public GroupsArtifactFilterExpert(GroupsArtifactFilter filter) {
		groups = filter.getGroups();
	}

	@Override
	public boolean matchesGroup(String groupId) {
		return groups.contains(groupId);
	}
	
	@Override
	public boolean matches(ArtifactIdentification identification) {
		return groups.contains(identification.getGroupId());
	}

	@Override
	public boolean matches(CompiledArtifactIdentification identification) {
		return groups.contains(identification.getGroupId());
	}

	@Override
	public boolean matches(CompiledPartIdentification identification) {
		return groups.contains(identification.getGroupId());
	}
}
