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
 * A {@link RepositorySelector} which selects repositories by matching their name against a {@link #getRegex() regular
 * expression}.
 *
 * @author michael.lafite
 */
public interface ByNameRegexRepositorySelector extends RepositorySelector {

	final EntityType<ByNameRegexRepositorySelector> T = EntityTypes.T(ByNameRegexRepositorySelector.class);

	String regex = "regex";

	/**
	 * The regular expression used to select repositories. If a {@link Repository#getName() repository's name} matches
	 * the regular expression, it will be selected.
	 */
	String getRegex();
	void setRegex(String regex);
}
