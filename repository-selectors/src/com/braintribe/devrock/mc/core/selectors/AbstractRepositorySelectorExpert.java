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