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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents the information about a repository 
 * 
 * @author xsi/pit
 *
 */
public interface RepositoryOrigin extends GenericEntity {
 
	EntityType<RepositoryOrigin> T = EntityTypes.T(RepositoryOrigin.class);
	
	/**
	 * the actual URL of the repository
	 * @return - the URL as a {@link String}
	 */
	String getUrl();
	/**
	 * the actual URL of the repository
	 * @param url - the URL as a String
	 */
	void setUrl(String url);
	
	/**
	 * the name or - in case of Maven - the ID of the repository
	 * @return - the name (or ID) of the repo
	 */
	String getName();
	/**
	 * the name or - in case of Maven - the ID of the repository
	 * @param name - the name (or ID) of the repo
	 */
	void setName(String name);
	
}
