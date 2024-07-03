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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents the information about a specific version, i.e. what repos can serve it
 * @author xsi/pit
 *
 */
public interface VersionInfo extends HasRepositoryOrigins {

	EntityType<VersionInfo> T = EntityTypes.T(VersionInfo.class);
	

	/**
	 * the version as a string
	 * @return - the version as a {@link String}
	 */
	String getVersion();
	/**
	 * the version as a string
	 * @param version - the version as a {@link String}
	 */
	void setVersion( String version);
	
}
