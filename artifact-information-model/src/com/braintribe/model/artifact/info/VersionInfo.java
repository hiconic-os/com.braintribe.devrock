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
package com.braintribe.model.artifact.info;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents the information about a specific version, i.e. what repos can serve it
 * @author xsi/pit
 *
 */
public interface VersionInfo extends HasRepositoryOrigins {

	EntityType<VersionInfo> T = EntityTypes.T(VersionInfo.class);
	

	/**
	 * the version as a string
	 * @return - the version as a {@link String}
	 */
	String getVersion();
	/**
	 * the version as a string
	 * @param version - the version as a {@link String}
	 */
	void setVersion( String version);
	
}
