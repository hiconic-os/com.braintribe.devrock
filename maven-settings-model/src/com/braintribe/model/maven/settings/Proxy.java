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
package com.braintribe.model.maven.settings;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;  

  
public interface Proxy extends com.braintribe.model.generic.GenericEntity {
	
	final EntityType<Proxy> T = EntityTypes.T(Proxy.class);

	public static final String active = "active";
	public static final String host = "host";
	public static final String id = "id";
	public static final String id1 = "id1";
	public static final String nonProxyHosts = "nonProxyHosts";
	public static final String password = "password";
	public static final String port = "port";
	public static final String protocol = "protocol";
	public static final String username = "username";

	void setActive(java.lang.Boolean value);
	java.lang.Boolean getActive();

	void setHost(java.lang.String value);
	java.lang.String getHost();


	void setNonProxyHosts(java.lang.String value);
	java.lang.String getNonProxyHosts();

	void setPassword(java.lang.String value);
	java.lang.String getPassword();

	void setPort(java.lang.Integer value);
	java.lang.Integer getPort();

	void setProtocol(java.lang.String value);
	java.lang.String getProtocol();

	void setUsername(java.lang.String value);
	java.lang.String getUsername();
	

}
