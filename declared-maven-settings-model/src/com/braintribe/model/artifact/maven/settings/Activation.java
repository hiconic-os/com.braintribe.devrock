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

  
public interface Activation extends com.braintribe.model.generic.GenericEntity {
	
	final EntityType<Activation> T = EntityTypes.T(Activation.class); 
	
	public static final String activeByDefault = "activeByDefault";
	public static final String file = "file";	
	public static final String jdk = "jdk";
	public static final String os = "os";
	public static final String property = "property";

	void setActiveByDefault(java.lang.Boolean value);
	java.lang.Boolean getActiveByDefault();

	void setFile(com.braintribe.model.artifact.maven.settings.ActivationFile value);
	com.braintribe.model.artifact.maven.settings.ActivationFile getFile();
	
	void setJdk(java.lang.String value);
	java.lang.String getJdk();

	void setOs(com.braintribe.model.artifact.maven.settings.ActivationOS value);
	com.braintribe.model.artifact.maven.settings.ActivationOS getOs();

	void setProperty(com.braintribe.model.artifact.maven.settings.ActivationProperty value);
	com.braintribe.model.artifact.maven.settings.ActivationProperty getProperty();

}
