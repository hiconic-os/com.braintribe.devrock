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
package com.braintribe.devrock.mc.api.resolver;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;

/**
 * interface that allows access to the current state of part-availability as reflected in the local repository. 
 * Note that only repositories backed by artifactory (our repositories) can return the FULL list of available parts,
 * whereas the others only return what they know at this point in time.
 * 
 * Only implementer right now is the beast, the LocalRepositoryCachingResolver
 * 
 * @author pit
 *
 */
public interface PartAvailabilityReflection {

	/**
	 * returns all currently known LOCAL information about the available parts of an artifact
	 * @param compiledArtifactIdentification - the {@link CompiledArtifactIdentification} that identifies the artifact
	 * @return - a {@link Map} of the id of the repository and a {@link Set} of {@link CompiledPartIdentification} 
	 */
	default List<PartReflection> getAvailablePartsOf( CompiledArtifactIdentification compiledArtifactIdentification) {
		return getAvailablePartsOfReasoned(compiledArtifactIdentification).get();
	}
	
	Maybe<List<PartReflection>> getAvailablePartsOfReasoned( CompiledArtifactIdentification compiledArtifactIdentification);
}
