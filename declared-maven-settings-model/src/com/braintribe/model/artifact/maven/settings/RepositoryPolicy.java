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
package com.braintribe.model.artifact.maven.settings;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;  


public interface RepositoryPolicy extends com.braintribe.model.generic.GenericEntity {
	
	final EntityType<RepositoryPolicy> T = EntityTypes.T(RepositoryPolicy.class);

	public static final String checksumPolicy = "checksumPolicy";
	public static final String enabled = "enabled";
	public static final String updatePolicy = "updatePolicy";

	void setChecksumPolicy(java.lang.String value);
	java.lang.String getChecksumPolicy();

	void setEnabled(java.lang.Boolean value);
	java.lang.Boolean getEnabled();


	void setUpdatePolicy(java.lang.String value);
	java.lang.String getUpdatePolicy();

}
