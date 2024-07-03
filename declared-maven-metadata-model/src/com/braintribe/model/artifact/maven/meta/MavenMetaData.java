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
