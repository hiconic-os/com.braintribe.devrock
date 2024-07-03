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
