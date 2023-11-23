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
package com.braintribe.devrock.model.mc.reason;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


@SelectiveInformation("Invalid combination of dependency type ${dependencyType} and solution packaging ${packaging} at resolution path ${pathInResolution}")
public interface ClasspathInvalidDependencyReference extends McReason {
	
	EntityType<ClasspathInvalidDependencyReference> T = EntityTypes.T(ClasspathInvalidDependencyReference.class);
	
	String packaging = "packaging";
	String dependencyType = "dependencyType";
	String pathInResolution = "pathInResolution";

	/**
	 * @return - the packaging of the dependency's solution
	 */
	String getPackaging();
	void setPackaging(String value);
	
	/**
	 * @return - the type of the dependency 
	 */
	String getDependencyType();
	void setDependencyType(String value);


	/**
	 * @return - the full path up to the dependency
	 */
	String getPathInResolution();
	void setPathInResolution(String value);

	
	
	

}
