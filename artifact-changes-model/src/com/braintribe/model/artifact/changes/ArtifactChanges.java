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
package com.braintribe.model.artifact.changes;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * the model to reflect the stored last time stamp of RH probing
 * @author pit / dirk
 *
 */
public interface ArtifactChanges extends GenericEntity{
		
	final EntityType<ArtifactChanges> T = EntityTypes.T(ArtifactChanges.class);
	String lastSynchronization = "lastSynchronization";
	String repositoryUrl = "repositoryUrl";
	String artifactIndexLevel = "artifactIndexLevel";
	
	/**
	 * @return - the {@link Date} stored as last successful RH synchronization
	 */
	Date getLastSynchronization();
	void setLastSynchronization(Date lastAccess);
	
	ArtifactIndexLevel getArtifactIndexLevel();
	void setArtifactIndexLevel(ArtifactIndexLevel artifactIndexLevel);
	
	/**
	 * @return - the URL of the repository (required by mc-legacy)
	 */
	String getRepositoryUrl();
	void setRepositoryUrl( String url);
}
