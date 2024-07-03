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
