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
	List<PartReflection> getPartsOf( CompiledArtifactIdentification compiledArtifactIdentification);
	

	/**
	 * accesses the repository connected to get whatever data structure containing the data of what the repository 
	 * has for the {@link CompiledArtifactIdentification}. In case of a stupid {@link MavenHttpRepository}, it'll be the 
	 * HTML produced on accessing the artifact's remote location, in case of Artifactory for instance, it's their JSON
	 * data... so what's in the resolution depends on the implementation
	 * @param compiledArtifactIdentification - the {@link CompiledArtifactIdentification}
	 * @return - {@link Optional} of {@link ArtifactDataResolution}
	 */
	Maybe<ArtifactDataResolution> getPartOverview( CompiledArtifactIdentification compiledArtifactIdentification);
}
