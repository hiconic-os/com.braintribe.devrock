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
import java.util.Map;
import java.util.Set;

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
	List<PartReflection> getAvailablePartsOf( CompiledArtifactIdentification compiledArtifactIdentification);
}
