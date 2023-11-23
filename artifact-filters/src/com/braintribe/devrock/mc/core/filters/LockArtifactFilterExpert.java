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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.devrock.model.repository.filters.LockArtifactFilter;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.utils.StringTools;

/**
 * Expert implementation for {@link LockArtifactFilter}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class LockArtifactFilterExpert implements ArtifactFilterExpert {

	// e.g. 'org.example:my-artifact#1.2.3'
	private Set<String> locks;
	// e.g. 'org.example:my-artifact'
	private Set<String> artifacts;
	// e.g. 'org.example'
	private Set<String> groups;

	public LockArtifactFilterExpert(LockArtifactFilter filter) {
		locks = new LinkedHashSet<>(filter.getLocks());
		// for matching on artifact level we also need the set of artifacts (i.e. without versions)
		artifacts = locks.stream().map(lock -> StringTools.getSubstringBefore(lock, "#")).collect(Collectors.toCollection(LinkedHashSet::new));
		// for matching on group level we also need the set of groups (i.e. without artifacts and versions)
		groups = locks.stream().map(lock -> StringTools.getSubstringBefore(lock, ":")).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public boolean matchesGroup(String groupId) {
		return groups.contains(groupId);
	}
	
	@Override
	public boolean matches(ArtifactIdentification identification) {
		String artifact = identification.getGroupId() + ":" + identification.getArtifactId();
		return artifacts.contains(artifact);
	}

	@Override
	public boolean matches(CompiledArtifactIdentification identification) {
		String lock = identification.getGroupId() + ":" + identification.getArtifactId() + "#" + identification.getVersion().asString();
		return locks.contains(lock);
	}

	@Override
	public boolean matches(CompiledPartIdentification identification) {
		return matches((CompiledArtifactIdentification) identification);
	}
}
