// ============================================================================
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
package com.braintribe.model.artifact.meta;

import java.util.List;

import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.generic.StandardIdentifiable;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface MavenMetaData extends StandardIdentifiable{
	
	final EntityType<MavenMetaData> T = EntityTypes.T(MavenMetaData.class);
	String groupId = "groupId";
	String artifactId = "artifactId";
	String version = "version";
	String modelVersion = "modelVersion";
	String versioning = "versioning";
	String plugins = "plugins";
	String mcComment = "mcComment";
	
	/**
	 * @return - the group id of the artifact
	 */
	String getGroupId();
	void setGroupId( String groupId);
	
	/**
	 * @return - the artifact id 
	 */
	String getArtifactId();
	void setArtifactId( String artifactId);
	
	/**
	 * @return - the version 
	 */
	Version getVersion();
	void setVersion( Version version);
	
	/**
	 * @return - the model version (Maven interna)
	 */
	String getModelVersion();
	void setModelVersion(String modelVersion);
	
	/**
	 * @return - the {@link Versioning} structure
	 */
	Versioning getVersioning();
	void setVersioning( Versioning versioning);
	
	/**
	 * @return - the list of {@link Plugin}
	 */
	List<Plugin> getPlugins();
	void setPlugins( List<Plugin> plugins);
	
	/**
	 * @return - malaclypse's comment while creating the file  
	 */
	String getMcComment();
	void setMcComment( String comment);
	 
}
