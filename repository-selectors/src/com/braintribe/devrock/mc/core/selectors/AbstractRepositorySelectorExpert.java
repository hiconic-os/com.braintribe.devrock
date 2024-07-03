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
package com.braintribe.devrock.mc.core.selectors;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repositoryview.RepositorySelector;

/**
 * Abstract super class for {@link RepositorySelector} experts.
 */
abstract class AbstractRepositorySelectorExpert implements RepositorySelectorExpert {

	@Override
	public final boolean selects(Repository repository) {
		validate(repository);
		return selectsWithoutValidation(repository);
	}

	protected abstract boolean selectsWithoutValidation(Repository repository);

	/**
	 * Validates the {@code Repository} which is passed and verifies that mandatory properties are set. Otherwise it
	 * throws a {@link IllegalArgumentException} with a meaningful error message.
	 */
	private void validate(Repository repository) {
		if (repository.getName() == null) {
			throw new IllegalArgumentException("The passed name for repository " + repository + " must not be null!");
		}
		if (repository.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("The passed name for repository " + repository + " must not be empty!");
		}
	}

}