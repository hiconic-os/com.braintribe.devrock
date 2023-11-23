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
package com.braintribe.devrock.mc.api.commons;

import com.braintribe.model.artifact.essential.PartIdentification;

/**
 * lill' helper to define the most commonly used {@link PartIdentification}
 * 
 * @author pit/dirk
 *
 */
public interface PartIdentifications {

	PartIdentification pom = PartIdentification.create( "pom");
	
	String pomPartKey = pom.asString();
	
	PartIdentification jar = PartIdentification.create( "jar");
	PartIdentification sources_jar = PartIdentification.create( "sources", "jar");
	PartIdentification javadoc_jar = PartIdentification.create( "javadoc", "jar");
	PartIdentification classes_jar = PartIdentification.create( "classes", "jar");
	PartIdentification md5 = PartIdentification.create( "md5");
	PartIdentification sha1 = PartIdentification.create( "sha1");
	PartIdentification asc = PartIdentification.create( "asc");
}
