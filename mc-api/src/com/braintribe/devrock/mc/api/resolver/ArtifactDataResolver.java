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


import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.devrock.model.repository.MavenHttpRepository;

import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;

/**
 * a combined resolver for both metadata and files
 * @author pit/dirk
 *
 */
public interface ArtifactDataResolver extends ArtifactResolver, ArtifactMetaDataResolver {
	
	/**
	 * actively accesses the repository connected to the resolve and get a list of parts
	 * @param compiledArtifactIdentification - the {@link CompiledArtifactIdentification} to get the parts of 
	 * @return - a {@link List} of {@link PartReflection}
	 */
	default List<PartReflection> getPartsOf( CompiledArtifactIdentification compiledArtifactIdentification) {
		return getPartsOfReasoned(compiledArtifactIdentification).get();
	}
	
	Maybe<List<PartReflection>> getPartsOfReasoned( CompiledArtifactIdentification compiledArtifactIdentification);

	/**
	 * accesses the repository connected to get whatever data structure containing the data of what the repository 
	 * has for the {@link CompiledArtifactIdentification}. In case of a stupid {@link MavenHttpRepository}, it'll be the 
	 * HTML produced on accessing the artifact's remote location, in case of Artifactory for instance, it's their JSON
	 * data... so what's in the resolution depends on the implementation
	 * @param compiledArtifactIdentification - the {@link CompiledArtifactIdentification}
	 */
	Maybe<ArtifactDataResolution> getPartOverview( CompiledArtifactIdentification compiledArtifactIdentification);
}
