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
package com.braintribe.devrock.model.repository;

import java.util.Set;

import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a repository that reflects (in our case here) an Eclipse workspace' content 
 * @author pit / dirk
 *
 */
public interface WorkspaceRepository extends Repository {
	
	EntityType<WorkspaceRepository> T = EntityTypes.T(WorkspaceRepository.class);
	
	String artifacts = "artifacts";

	
	Set<Artifact> getArtifacts();
	void setArtifacts( Set<Artifact> artifacts);
	
}
