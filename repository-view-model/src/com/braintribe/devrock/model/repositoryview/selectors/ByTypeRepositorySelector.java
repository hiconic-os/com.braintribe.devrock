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

import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repositoryview.RepositorySelector;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A {@link RepositorySelector} which selects repositories by {@link #getType() type}.
 *
 * @author michael.lafite
 */
public interface ByTypeRepositorySelector extends RepositorySelector {

	final EntityType<ByTypeRepositorySelector> T = EntityTypes.T(ByTypeRepositorySelector.class);

	String type = "type";
	String includeSubtypes = "includeSubtypes";

	/**
	 * The short type name of the repository to select, e.g. {@link MavenHttpRepository}.
	 */
	String getType();
	void setType(String type);

	/**
	 * Whether or not to also include subtypes of the specified {@link #getType()}.
	 */
	@Initializer("true")
	Boolean getIncludeSubtypes();
	void setIncludeSubtypes(Boolean includeSubtypes);
}
