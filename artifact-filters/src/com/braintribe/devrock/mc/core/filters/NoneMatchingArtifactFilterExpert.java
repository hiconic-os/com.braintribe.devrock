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

import com.braintribe.devrock.model.repository.filters.NoneMatchingArtifactFilter;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * Expert implementation for {@link NoneMatchingArtifactFilter}. Since this filter (expert) doesn't match anything, it
 * requires no configuration and thus a single {@link #instance} is sufficient.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class NoneMatchingArtifactFilterExpert implements ArtifactFilterExpert {

	public static final NoneMatchingArtifactFilterExpert instance = new NoneMatchingArtifactFilterExpert();

	private NoneMatchingArtifactFilterExpert() {
		// nothing to do
	}

	@Override
	public boolean matchesGroup(String groupId) {
		return false;
	}	
	
	@Override
	public boolean matches(ArtifactIdentification artifactIdentification) throws IllegalArgumentException {
		return false;
	}

	@Override
	public boolean matches(CompiledArtifactIdentification compiledArtifactIdentification) {
		return false;
	}

	@Override
	public boolean matches(CompiledPartIdentification compiledPartIdentification) {
		return false;
	}
}
