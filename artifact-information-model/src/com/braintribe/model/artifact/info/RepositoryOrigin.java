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
package com.braintribe.model.artifact.info;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents the information about a repository 
 * 
 * @author xsi/pit
 *
 */
public interface RepositoryOrigin extends GenericEntity {
 
	EntityType<RepositoryOrigin> T = EntityTypes.T(RepositoryOrigin.class);
	
	/**
	 * the actual URL of the repository
	 * @return - the URL as a {@link String}
	 */
	String getUrl();
	/**
	 * the actual URL of the repository
	 * @param url - the URL as a String
	 */
	void setUrl(String url);
	
	/**
	 * the name or - in case of Maven - the ID of the repository
	 * @return - the name (or ID) of the repo
	 */
	String getName();
	/**
	 * the name or - in case of Maven - the ID of the repository
	 * @param name - the name (or ID) of the repo
	 */
	void setName(String name);
	
}
