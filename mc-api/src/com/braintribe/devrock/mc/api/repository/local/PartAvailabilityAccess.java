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
package com.braintribe.devrock.mc.api.repository.local;

import java.util.Set;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.version.Version;

/**
 * 
 * @author pit / dirk
 *
 */
public interface PartAvailabilityAccess {
	/**
	 * @param partIdentification - the {@link PartIdentification} that identifies the desired part 
	 * @return - the {@link PartAvailability} of this part 
	 */
	PartAvailability getAvailability( PartIdentification partIdentification);
	
	/**
	 * @param partIdentification - the {@link PartIdentification} that identifies the desired part
	 * @param availablity - the {@link PartAvailability} to store for this part
	 */
	void setAvailablity( PartIdentification partIdentification, PartAvailability availablity);
	
	
	/**
	 * @return - the actual version (in case of snapshot the 'expected' version of such a part, otherwise the version as it was)
	 */
	Version getActualVersion();
	
	
	/**
	 * @return - the {@link Repository} the access is bound to 
	 */
	Repository repository();
	
	/**
	 * @return - the {@link ArtifactPartResolverPersistenceDelegate} attached
	 */
	ArtifactPartResolverPersistenceDelegate repoDelegate();
	
	
	/**
	 * @return - a {@link Set} of {@link PartIdentification} that are currently known to be available
	 */
	Set<CompiledPartIdentification> getAvailableParts();	
	
}
