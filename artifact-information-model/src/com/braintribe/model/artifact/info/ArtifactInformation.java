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
package com.braintribe.model.artifact.info;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * contains information about this artifact and its parts
 * 
 * @author pit
 *
 */
public interface ArtifactInformation extends GenericEntity {
	
	final EntityType<ArtifactInformation> T = EntityTypes.T(ArtifactInformation.class);
	
	String getGroupId();
	void setGroupId( String groupId);
	
	String getArtifactId();
	void setArtifactId( String artifactId);
	
	String getVersion();
	void setVersion( String version);
	
	LocalRepositoryInformation getLocalInformation();
	void setLocalInformation( LocalRepositoryInformation localInformation);
	
	List<RemoteRepositoryInformation> getRemoteInformation();
	void setRemoteInformation( List<RemoteRepositoryInformation> remoteInformation);
	
	
}
