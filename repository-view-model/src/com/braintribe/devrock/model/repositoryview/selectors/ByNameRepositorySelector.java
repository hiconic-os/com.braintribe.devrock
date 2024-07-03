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
package com.braintribe.devrock.model.repositoryview.selectors;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repositoryview.RepositorySelector;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A {@link RepositorySelector} which selects repositories by {@link #getName() name}.
 *
 * @author michael.lafite
 */
public interface ByNameRepositorySelector extends RepositorySelector {

	final EntityType<ByNameRepositorySelector> T = EntityTypes.T(ByNameRepositorySelector.class);

	String name = "name";

	/**
	 * The name of the repository to select, i.e. this will be compared to the {@link Repository#getName() repository
	 * name}.
	 */
	String getName();
	void setName(String name);
}
