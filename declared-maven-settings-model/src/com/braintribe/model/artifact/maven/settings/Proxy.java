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

  
public interface Proxy extends com.braintribe.model.generic.GenericEntity {
	
	final EntityType<Proxy> T = EntityTypes.T(Proxy.class);

	public static final String active = "active";
	public static final String host = "host";	
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
