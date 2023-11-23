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
package com.braintribe.model.artifact.meta;

import com.braintribe.model.generic.GenericEntity;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * a representation of whatever Maven writes in these tags.. 
 * @author pit
 *
 */
public interface Plugin extends GenericEntity {

	final EntityType<Plugin> T = EntityTypes.T(Plugin.class);
	
	String getName();
	void setName( String name);
	
	String getPrefix();
	void setPrefix( String prefix);
	
	String getArtifactId();
	void setArtifactId( String artifactId);
}
