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

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * a resolver for maven-metadata files (both unversioned and versioned artifact level)
 * @author pit / dirk
 *
 */
public interface ArtifactMetaDataResolver {
	/**
	 * resolves the metadata for unversioned artifact 
	 * @param identification - {@link ArtifactIdentification}
	 * @return - the respective {@link ArtifactDataResolution}
	 */
	Maybe<ArtifactDataResolution> resolveMetadata( ArtifactIdentification identification);
	
	/**
	 * resolves the metadata for versioned artifact
	 * @param identification - the {@link CompiledArtifactIdentification}
	 * @return - the respective {@link ArtifactDataResolution}
	 */
	Maybe<ArtifactDataResolution> resolveMetadata( CompiledArtifactIdentification identification);	
}
