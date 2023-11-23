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

import static com.braintribe.devrock.mc.core.selectors.RepositorySelectorAssertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.braintribe.devrock.model.repository.CodebaseRepository;
import com.braintribe.devrock.model.repository.LocalRepository;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.MavenRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repositoryview.RepositorySelector;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Provides tests for {@link ByNameRepositorySelectorExpert}, {@link ByNameRegexRepositorySelectorExpert} and
 * {@link ByTypeRepositorySelectorExpert} experts.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class RepositorySelectorExpertTest extends AbstractRepositorySelectorExpertTest {

	@Test
	public void test() {
		// @formatter:off
		// test name selector
		assertThat(nameSelector("")).selectsNone(createRepositories("core-dev", "core-stable", "name-1", "name 2"));
		assertThat(nameSelector("core-dev")).selectsAll(createRepositories("core-dev"));
		assertThat(nameSelector("core-dev")).selectsNone(createRepositories("core-stable", "name-1", "name 2"));

		// test regex name selector
		assertThat(regexNameSelector("")).selectsNone(createRepositories("core-dev", "core-stable", "name-1", "name 2"));
		assertThat(regexNameSelector("core-dev")).selectsAll(createRepositories("core-dev"));
		assertThat(regexNameSelector("core.*")).selectsAll(createRepositories("core-dev", "core-stable", "core-stable20200520"));
		assertThat(regexNameSelector("core.*")).selectsNone(createRepositories("dev-core-stable", "name-1", "name 2"));

		assertThat(typeSelector(MavenHttpRepository.T, false)).selectsAll(createRepositories(MavenHttpRepository.T, "some-repo"));
		assertThat(typeSelector(MavenHttpRepository.T, true)).selects(createRepository(MavenHttpRepository.T, "some-repo"));

		assertThat(typeSelector(Repository.T, true)).selectsAll(
			CommonTools.getList(
				createRepository(MavenHttpRepository.T, "some-repo"),
				createRepository(MavenRepository.T, "some-repo"),
				createRepository(CodebaseRepository.T, "some-repo"),
				createRepository(LocalRepository.T, "some-repo"),
				createRepository(MavenFileSystemRepository.T, "some-repo")
			));
		
		final RepositorySelector typeSelector = typeSelector(MavenRepository.T, true);
		assertThat(typeSelector).selectsAll(
			CommonTools.getList(
				createRepository(MavenHttpRepository.T, "some-repo"),
				createRepository(MavenRepository.T, "some-repo"),
				createRepository(MavenFileSystemRepository.T, "some-repo")
			));
		
		assertThat(typeSelector).selectsNone(
			CommonTools.getList(
				createRepository(CodebaseRepository.T, "some-repo"),
				createRepository(LocalRepository.T, "some-repo")
			));
	
		assertThat(typeSelector(MavenHttpRepository.T, true)).selectsNone(
			CommonTools.getList(
				createRepository(MavenRepository.T, "some-repo"),
				createRepository(CodebaseRepository.T, "some-repo"),
				createRepository(LocalRepository.T, "some-repo"),
				createRepository(MavenFileSystemRepository.T, "some-repo")
			));

		List<Repository> expectedSelectedRepositories = createRepositories(MavenHttpRepository.T, "maven-http-1", "maven-http-2", "maven-http-3"); 
		expectedSelectedRepositories.addAll(createRepositories(MavenRepository.T, "maven-1", "maven-2", "maven-3"));
		expectedSelectedRepositories.addAll(createRepositories(MavenFileSystemRepository.T, "maven-file-1", "maven-file-2", "maven-file-3"));
		List<Repository> allRepositories = createRepositories(CodebaseRepository.T, "codebase-1", "codebase-2");  
		allRepositories.addAll(createRepositories(LocalRepository.T, "local-1", "local-2"));
		allRepositories.addAll(expectedSelectedRepositories);
		assertThat(typeSelector).selectsExactly(allRepositories, expectedSelectedRepositories);
		
		// @formatter:on
	}
}
