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
