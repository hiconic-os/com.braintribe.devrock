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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assert;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repositoryview.RepositorySelector;
import com.braintribe.testing.junit.assertions.assertj.core.api.SharedAssert;

/**
 * {@link Assert} implementation for {@link RepositorySelector} assertions. The respective
 * {@link RepositorySelectorExpert}s are created in the background using
 * {@link RepositorySelectors#forDenotation(RepositorySelector)}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class RepositorySelectorAssert extends AbstractObjectAssert<RepositorySelectorAssert, RepositorySelector>
		implements SharedAssert<RepositorySelectorAssert, RepositorySelector> {

	private RepositorySelectorExpert expert;

	public RepositorySelectorAssert(RepositorySelector actual) {
		super(actual, RepositorySelectorAssert.class);
		this.expert = RepositorySelectors.forDenotation(actual);
	}

	public RepositorySelectorAssert selectsAll(List<Repository> repositories) {
		for (Repository repository : repositories) {
			if (!expert.selects(repository)) {
				failWithMessage("Expert " + expert + " unexpectedly does not match " + repository + "!");
			}
		}
		return this;
	}

	public RepositorySelectorAssert selectsExactly(List<Repository> allRepositories, List<Repository> expectedSelectedRepositories) {
		List<Repository> actualSelectedRepositories = allRepositories.stream().filter(expert::selects).collect(Collectors.toList());
		assertThat(actualSelectedRepositories).isEqualTo(expectedSelectedRepositories);
		return this;
	}

	public RepositorySelectorAssert selectsNone(List<Repository> repositories) {
		for (Repository repository : repositories) {
			if (expert.selects(repository)) {
				failWithMessage("Expert " + expert + " unexpectedly matches " + repository + "!");
			}
		}
		return this;
	}
	
	public RepositorySelectorAssert selects(Repository repository) {	
		if (!expert.selects(repository)) {
			failWithMessage("Expert " + expert + " unexpectedly does not match " + repository + "!");
		}
		return this;
	}
}
