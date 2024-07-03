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


public interface Server extends com.braintribe.model.generic.GenericEntity {
	
	final EntityType<Server> T = EntityTypes.T(Server.class);

	public static final String configuration = "configuration";
	public static final String directoryPermissions = "directoryPermissions";
	public static final String filePermissions = "filePermissions";	
	public static final String passphrase = "passphrase";
	public static final String password = "password";
	public static final String privateKey = "privateKey";
	public static final String username = "username";

	void setConfiguration(com.braintribe.model.artifact.maven.settings.Configuration value);
	com.braintribe.model.artifact.maven.settings.Configuration getConfiguration();

	void setDirectoryPermissions(java.lang.String value);
	java.lang.String getDirectoryPermissions();

	void setFilePermissions(java.lang.String value);
	java.lang.String getFilePermissions();

	void setPassphrase(java.lang.String value);
	java.lang.String getPassphrase();

	void setPassword(java.lang.String value);
	java.lang.String getPassword();

	void setPrivateKey(java.lang.String value);
	java.lang.String getPrivateKey();

	void setUsername(java.lang.String value);
	java.lang.String getUsername();

}
