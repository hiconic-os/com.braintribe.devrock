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
