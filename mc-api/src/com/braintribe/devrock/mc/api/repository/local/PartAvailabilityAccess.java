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
