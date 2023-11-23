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
package com.braintribe.devrock.model.repolet.content;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * the container of all repolet relevant data 
 * @author pit
 *
 */
public interface RepoletContent extends GenericEntity {	
	EntityType<RepoletContent> T = EntityTypes.T(RepoletContent.class);
	
	String artifacts = "artifacts";
	String repositoryId = "repositoryId";

	/**
	 * @return - a {@link List} of {@link Artifact} that are contained
	 */
	List<Artifact> getArtifacts();
	void setArtifacts(List<Artifact> value);
	
	/**
	 * @return - the repository id (used for local contents, null for remote)
	 */
	String getRepositoryId();
	void setRepositoryId(String value);

}
