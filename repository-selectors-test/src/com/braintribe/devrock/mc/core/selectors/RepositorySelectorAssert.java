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
