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
package com.braintribe.model.artifact.maven.meta;

import java.util.List;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.version.Version;

/**
 * represents a maven-metadata file content
 * 
 * @author pit
 *
 */
public interface MavenMetaData extends StandardIdentifiable{
	
	final EntityType<MavenMetaData> T = EntityTypes.T(MavenMetaData.class);
	
	/**
	 * @return - the groupId ( may not be null) 
	 */
	String getGroupId();
	void setGroupId( String groupId);
	
	/**
	 * @return - the artifactId (may not be null) 
	 */
	String getArtifactId();
	void setArtifactId( String artifactId);
	
	/**
	 * @return - the associated version if any (depends on use-case)
	 */
	Version getVersion();
	void setVersion( Version version);
	
	/**
	 * @return - the model version as parse
	 */
	String getModelVersion();
	void setModelVersion(String modelVersion);
	
	/**
	 * @return - the versioning information if any (depends on use-case)
	 */
	Versioning getVersioning();
	void setVersioning( Versioning versioning);
	
	/**
	 * @return - the {@link Plugin}
	 */
	List<Plugin> getPlugins();
	void setPlugins( List<Plugin> plugins);
	 
}
