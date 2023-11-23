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

import com.braintribe.devrock.model.repositoryview.RepositorySelector;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A {@link RepositorySelector} that creates the negation of its {@link #getOperand() delegate selector}.
 *
 * @author michael.lafite
 */
public interface NegationRepositorySelector extends RepositorySelector {

	EntityType<NegationRepositorySelector> T = EntityTypes.T(NegationRepositorySelector.class);

	String operand = "operand";

	/**
	 * The selector to negate, i.e. if a repository is selected by the delegate, it is not selected by this selector
	 * (and vice-versa).
	 */
	RepositorySelector getOperand();
	void setOperand(RepositorySelector operand);
}
