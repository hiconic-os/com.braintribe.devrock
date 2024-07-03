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
import com.braintribe.devrock.model.repositoryview.selectors.NegationRepositorySelector;

/**
 * Expert implementation for {@link NegationRepositorySelector}.
 */
public class NegationRepositorySelectorExpert extends AbstractRepositorySelectorExpert {

	private RepositorySelectorExpert expert;

	public NegationRepositorySelectorExpert(NegationRepositorySelector repositorySelector) {
		this.expert = RepositorySelectors.forDenotationRecursively(repositorySelector.getOperand());
	}

	@Override
	public boolean selectsWithoutValidation(Repository repository) {
		return !expert.selects(repository);
	}
}
