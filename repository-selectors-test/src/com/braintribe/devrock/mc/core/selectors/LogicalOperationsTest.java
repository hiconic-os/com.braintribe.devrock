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
import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThatExecuting;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.braintribe.devrock.model.repository.CodebaseRepository;
import com.braintribe.devrock.model.repository.LocalRepository;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.MavenRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repositoryview.RepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.AllMatchingRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ConjunctionRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.DisjunctionRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.NegationRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.NoneMatchingRepositorySelector;
import com.braintribe.testing.junit.assertions.assertj.core.api.MethodInvocationAssert.ExecutableWithReturnValue;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Provides tests for {@link ConjunctionRepositorySelectorExpert}, {@link DisjunctionRepositorySelectorExpert} and
 * {@link NegationRepositorySelectorExpert}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class LogicalOperationsTest extends AbstractRepositorySelectorExpertTest {

	protected RepositorySelector TRUE = AllMatchingRepositorySelector.T.create();
	protected RepositorySelector FALSE = NoneMatchingRepositorySelector.T.create();

	protected static ConjunctionRepositorySelector and(RepositorySelector... operands) {
		ConjunctionRepositorySelector result = ConjunctionRepositorySelector.T.create();
		result.setOperands(Arrays.asList(operands));
		return result;
	}

	protected static DisjunctionRepositorySelector or(RepositorySelector... operands) {
		DisjunctionRepositorySelector result = DisjunctionRepositorySelector.T.create();
		result.setOperands(Arrays.asList(operands));
		return result;
	}

	protected static NegationRepositorySelector not(RepositorySelector operand) {
		NegationRepositorySelector result = NegationRepositorySelector.T.create();
		result.setOperand(operand);
		return result;
	}

	@Test
	public void test() {

		// @formatter:off

		List<Repository> repositories = CommonTools.getList(
				createRepository(MavenHttpRepository.T, "some-repo"),
				createRepository(MavenRepository.T, "some-repo"),
				createRepository(CodebaseRepository.T, "some-repo"),
				createRepository(LocalRepository.T, "some-repo"),
				createRepository(MavenFileSystemRepository.T, "some-repo")
			);

		repositories.addAll(
			createRepositories(
				"core-dev", 
				"core-stable", 
				"name-1", 
				"name-2"));

		assertThat(
				and()
			).selectsAll(repositories);

		assertThat(
				and(TRUE)
			).selectsAll(repositories);

		assertThat(
				and(TRUE, TRUE)
			).selectsAll(repositories);

		assertThat(
				and(TRUE, TRUE, TRUE, TRUE, TRUE)
			).selectsAll(repositories);

		assertThat(
				and(FALSE, TRUE)
			).selectsNone(repositories);

		assertThat(
				and(TRUE, FALSE)
			).selectsNone(repositories);

		assertThat(
				and(FALSE, FALSE)
			).selectsNone(repositories);

		assertThat(
				and(FALSE, TRUE, TRUE, TRUE, TRUE)
			).selectsNone(repositories);

		assertThat(
				and(TRUE, TRUE, TRUE, TRUE, FALSE)
			).selectsNone(repositories);

		assertThat(
				or()
			).selectsNone(repositories);

		assertThat(
				or(FALSE)
			).selectsNone(repositories);

		assertThat(
				or(FALSE, FALSE)
			).selectsNone(repositories);

		assertThat(
				or(FALSE, FALSE,FALSE, FALSE,FALSE)
			).selectsNone(repositories);

		assertThat(
				or(FALSE, TRUE)
			).selectsAll(repositories);

		assertThat(
				or(TRUE, FALSE)
			).selectsAll(repositories);

		assertThat(
				or(TRUE, TRUE)
			).selectsAll(repositories);

		assertThat(
				or(TRUE, TRUE, TRUE, TRUE, TRUE)
			).selectsAll(repositories);

		assertThat(
				or(FALSE, TRUE, TRUE, TRUE, TRUE)
			).selectsAll(repositories);

		assertThat(
				or(TRUE, TRUE, TRUE, TRUE, FALSE)
			).selectsAll(repositories);


		assertThat(
				not(FALSE)
			).selectsAll(repositories);

		assertThat(
				not(TRUE)
			).selectsNone(repositories);

		assertThat(
				or(
					and(TRUE,FALSE),
					not(
						and(TRUE,FALSE)
					)
				)
			).selectsAll(repositories);
		
		// we allow empty junctions, but null filters are never allowed (except on root level where we interpret as match-all)
		List<ExecutableWithReturnValue<?>> methodInvocationsWithNullSelectors = CommonTools.getList(
			() -> RepositorySelectors.forDenotation(not(null)),
			() -> RepositorySelectors.forDenotation(not(not(null))),
			() -> RepositorySelectors.forDenotation(and((RepositorySelector)null)),
			() -> RepositorySelectors.forDenotation(and((RepositorySelector)null, (RepositorySelector)null)),
			() -> RepositorySelectors.forDenotation(and(TRUE, (RepositorySelector)null, TRUE)),
			() -> RepositorySelectors.forDenotation(or((RepositorySelector)null)),
			() -> RepositorySelectors.forDenotation(or((RepositorySelector)null, (RepositorySelector)null)),
			() -> RepositorySelectors.forDenotation(or(FALSE, (RepositorySelector)null, FALSE)),
			() -> RepositorySelectors.forDenotation(or(TRUE,and(FALSE, (RepositorySelector)null)))
		);
		// @formatter:on

		for (ExecutableWithReturnValue<?> methodInvocationsWithNullSelector : methodInvocationsWithNullSelectors) {
			assertThatExecuting(methodInvocationsWithNullSelector).fails().throwingExceptionWhich().isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("unspecified repository selector");
		}
	}

}
