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
