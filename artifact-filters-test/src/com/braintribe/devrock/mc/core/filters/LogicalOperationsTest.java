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
package com.braintribe.devrock.mc.core.filters;

import static com.braintribe.devrock.mc.core.filters.ArtifactFilterAssertions.assertThat;
import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThatExecuting;

import java.util.List;

import org.junit.Test;

import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.testing.junit.assertions.assertj.core.api.MethodInvocationAssert.ExecutableWithReturnValue;
import com.braintribe.utils.CollectionTools;

/**
 * Provides tests for {@link ConjunctionArtifactFilterExpert}, {@link DisjunctionArtifactFilterExpert} and
 * {@link NegationArtifactFilterExpert}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class LogicalOperationsTest extends AbstractArtifactFilterExpertTest {

	@Test
	public void test() {
		ArtifactFilter TRUE = all();
		ArtifactFilter FALSE = none();

		// @formatter:off

		Object[] identifications = new Object[] {
				gi("com.braintribe.common"),
				ai("com.braintribe.common", "my-artifact"),
				cai("com.braintribe.common", "my-artifact", "1.2.3"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", null, null),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", "sources", "jar")
			};

		assertThat(
				and()
			).matchesAll(identifications);

		assertThat(
				and(TRUE)
			).matchesAll(identifications);

		assertThat(
				and(TRUE, TRUE)
			).matchesAll(identifications);

		assertThat(
				and(TRUE, TRUE, TRUE, TRUE, TRUE)
			).matchesAll(identifications);

		assertThat(
				and(FALSE, TRUE)
			).matchesNone(identifications);

		assertThat(
				and(TRUE, FALSE)
			).matchesNone(identifications);

		assertThat(
				and(FALSE, FALSE)
			).matchesNone(identifications);

		assertThat(
				and(FALSE, TRUE, TRUE, TRUE, TRUE)
			).matchesNone(identifications);

		assertThat(
				and(TRUE, TRUE, TRUE, TRUE, FALSE)
			).matchesNone(identifications);


		assertThat(
				or()
			).matchesNone(identifications);

		assertThat(
				or(FALSE)
			).matchesNone(identifications);

		assertThat(
				or(FALSE, FALSE)
			).matchesNone(identifications);

		assertThat(
				or(FALSE, FALSE,FALSE, FALSE,FALSE)
			).matchesNone(identifications);

		assertThat(
				or(FALSE, TRUE)
			).matchesAll(identifications);

		assertThat(
				or(TRUE, FALSE)
			).matchesAll(identifications);

		assertThat(
				or(TRUE, TRUE)
			).matchesAll(identifications);

		assertThat(
				or(TRUE, TRUE, TRUE, TRUE, TRUE)
			).matchesAll(identifications);

		assertThat(
				or(FALSE, TRUE, TRUE, TRUE, TRUE)
			).matchesAll(identifications);

		assertThat(
				or(TRUE, TRUE, TRUE, TRUE, FALSE)
			).matchesAll(identifications);


		assertThat(
				not(FALSE)
			).matchesAll(identifications);

		assertThat(
				not(TRUE)
			).matchesNone(identifications);


		assertThat(
				or(
					and(TRUE,FALSE),
					not(
						and(TRUE,FALSE)
					)
				)
			).matchesAll(identifications);

		// we allow empty junctions, but null filters are never allowed (except on root level where we interpret as match-all)
		List<ExecutableWithReturnValue<?>> methodInvocationsWithNullFilters = CollectionTools.getList(
				() -> ArtifactFilters.forDenotation(not(null)),
				() -> ArtifactFilters.forDenotation(not(not(null))),
				() -> ArtifactFilters.forDenotation(and((ArtifactFilter)null)),
				() -> ArtifactFilters.forDenotation(and((ArtifactFilter)null, (ArtifactFilter)null)),
				() -> ArtifactFilters.forDenotation(and(TRUE, (ArtifactFilter)null, TRUE)),
				() -> ArtifactFilters.forDenotation(or((ArtifactFilter)null)),
				() -> ArtifactFilters.forDenotation(or((ArtifactFilter)null, (ArtifactFilter)null)),
				() -> ArtifactFilters.forDenotation(or(FALSE, (ArtifactFilter)null, FALSE)),
				() -> ArtifactFilters.forDenotation(or(TRUE,and(FALSE, (ArtifactFilter)null))
			));
		// @formatter:on

		for (ExecutableWithReturnValue<?> methodInvocationsWithNullFilter : methodInvocationsWithNullFilters) {
			assertThatExecuting(methodInvocationsWithNullFilter).fails().throwingExceptionWhich().isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("unspecified filter");
		}
	}

}
