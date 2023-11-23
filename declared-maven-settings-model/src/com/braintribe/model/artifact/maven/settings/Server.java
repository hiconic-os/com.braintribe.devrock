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
