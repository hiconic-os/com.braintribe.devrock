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
package com.braintribe.devrock.model.repository;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface RepositoryConfiguration extends GenericEntity{
	
	EntityType<RepositoryConfiguration> T = EntityTypes.T(RepositoryConfiguration.class);
	
	String relevantRepositories = "relevantRepositories";
	String localRepositoryPath = "localRepositoryPath";

	/**
	 * @return - a {@link List} of {@link Repository} that are relevant
	 */
	List<Repository> getRelevantRepositories();
	void setRelevantRepositories(List<Repository> value);
	
	String getLocalRepositoryPath();
	void setLocalRepositoryPath(String value);


}
