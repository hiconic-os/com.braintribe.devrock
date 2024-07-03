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
