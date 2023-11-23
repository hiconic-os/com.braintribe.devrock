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

  
public interface ActivationOS extends com.braintribe.model.generic.GenericEntity {
	
	final EntityType<ActivationOS> T = EntityTypes.T(ActivationOS.class);
	
	public static final String arch = "arch";
	public static final String family = "family";
	
	public static final String name = "name";
	public static final String version = "version";

	void setArch(java.lang.String value);
	java.lang.String getArch();

	void setFamily(java.lang.String value);
	java.lang.String getFamily();


	void setName(java.lang.String value);
	java.lang.String getName();

	void setVersion(java.lang.String value);
	java.lang.String getVersion();

}
