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

import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * Defines the API for {@link ArtifactFilter} experts.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public interface ArtifactFilterExpert {

	/**
	 * ATTENTION: This method is just experimental. It may be changed or removed anytime.<br>
	 * Returns whether this filter matches the group specified via the passed <code>groupId</code>.
	 */
	boolean matchesGroup(String groupId);

	/**
	 * Returns whether this filter matches the passed <code>artifactIdentification</code>. The passed
	 * <code>artifactIdentification</code> and its {@link ArtifactIdentification#getGroupId() group id} and
	 * {@link ArtifactIdentification#getArtifactId() artifact id} are expected to be set.
	 */
	boolean matches(ArtifactIdentification artifactIdentification);

	/**
	 * Returns whether this filter matches the passed <code>compiledArtifactIdentification</code>. The passed
	 * <code>compiledArtifactIdentification</code> and its {@link CompiledArtifactIdentification#getGroupId() group id},
	 * {@link CompiledArtifactIdentification#getArtifactId() artifact id} and
	 * {@link CompiledArtifactIdentification#getVersion() version} are expected to be set.
	 */
	boolean matches(CompiledArtifactIdentification compiledArtifactIdentification);

	/**
	 * Returns whether this filter matches the passed <code>compiledPartIdentification</code>. The passed
	 * <code>compiledPartIdentification</code> and its {@link CompiledPartIdentification#getGroupId() group id},
	 * {@link CompiledPartIdentification#getArtifactId() artifact id}, or {@link CompiledPartIdentification#getVersion()
	 * version} are expected to be set. The {@link CompiledPartIdentification#getClassifier() classifier} and
	 * {@link CompiledPartIdentification#getType() type} are optional though. If they are not set, i.e.
	 * <code>null</code>, they are handled as empty strings. For example, a filter that matches an empty
	 * <code>type</code> string also matches <code>null</code>.
	 */
	boolean matches(CompiledPartIdentification compiledPartIdentification);
}
