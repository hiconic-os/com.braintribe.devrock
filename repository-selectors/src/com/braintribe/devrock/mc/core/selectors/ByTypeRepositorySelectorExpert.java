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
import com.braintribe.devrock.model.repositoryview.selectors.ByTypeRepositorySelector;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Expert implementation for {@link ByTypeRepositorySelector}.
 */
public class ByTypeRepositorySelectorExpert extends AbstractRepositorySelectorExpert {

	private boolean includeSubtypes;
	private EntityType<? extends Repository> entityType; // MavenRepository

	public ByTypeRepositorySelectorExpert(ByTypeRepositorySelector repositorySelector) {
		if(repositorySelector.getIncludeSubtypes() == null) {
			throw new IllegalArgumentException(ByTypeRepositorySelector.includeSubtypes + " must not be null!");
		}
		this.includeSubtypes = repositorySelector.getIncludeSubtypes();
		this.entityType = (EntityType<? extends Repository>) EntityTypes.get(Repository.class.getPackage().getName() + "." + repositorySelector.getType());
	}

	@Override
	public boolean selectsWithoutValidation(Repository repository) {
		EntityType<? extends Repository> repositoryType = repository.entityType();

		if (this.includeSubtypes) {
			return entityType.isAssignableFrom(repositoryType);
		}
		return repositoryType.equals(repositoryType);
	}
}
