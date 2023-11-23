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
