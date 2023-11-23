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

import org.junit.Test;

/**
 * Provides tests for {@link QualifiedArtifactFilterExpert}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class QualifiedArtifactFilterExpertTest extends AbstractArtifactFilterExpertTest {

	@Test
	public void test() {
		// @formatter:off

		// test null matches everything
		assertThat(qualified(null, null, null, null, null)).matchesAll(
				gi("com.braintribe.common"),
				ai("com.braintribe.common", "my-artifact"),
				cai("com.braintribe.common", "my-artifact", "1.2.3"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", null, null),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", "sources", null),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", null, "jar"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", "sources", "jar")
			);

		// test group match
		assertThat(qualified("com.braintribe.common", null, null, null, null)).matchesAll(
				gi("com.braintribe.common"),
				ai("com.braintribe.common", "my-artifact"),
				ai("com.braintribe.common", "some-other-artifact"),
				cai("com.braintribe.common", "my-artifact", "1.2.3"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", null, null)
			).matchesNone(
				gi("com.braintribe"),
				gi("com.braintribe.common2"),
				gi("com.braintribe.common.subgroup"),
				ai("com.braintribe", "my-artifact"),
				ai("com.braintribe.othergroup", "my-artifact"),
				cai("com.braintribe", "my-artifact", "1.2.3"),
				cpi("com.braintribe", "my-artifact", "1.2.3", null, null)
			);

		// test single wildcard
		assertThat(qualified("com.braintribe.*", null, null, null, null)).matchesAll(
				gi("com.braintribe.common"),
				gi("com.braintribe.common.subgroup"),
				ai("com.braintribe.common", "my-artifact"),
				ai("com.braintribe.common.subgroup", "my-artifact"),
				ai("com.braintribe.anygroup", "my-artifact"),
				cai("com.braintribe.common", "my-artifact", "1.2.3"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", null, null)
			).matchesNone(
				gi("com.braintribe"),
				ai("com.braintribe", "my-artifact")
			);

		// test multiple wildcards
		assertThat(qualified("*ab*cd*ef*", null, null, null, null)).matchesAll(
				gi("abcdef"),
				gi("anyabanycdanyefany"),
				ai("abcdef", "my-artifact"),
				ai("anyabanycdanyefany", "my-artifact"),
				ai("abcdabxcdef", "my-artifact"),
				ai("foo.xabx.xcdx.xef.bar", "my-artifact")
			).matchesNone(
				gi("abcxef"),
				ai("abcxef", "my-artifact")
			);

		// test version range
		assertThat(qualified("com.braintribe.common", "platform-api", "[1.2.4,1.2.6]", null, null)).matchesAll(
				gi("com.braintribe.common"),
				ai("com.braintribe.common", "platform-api"),
				cai("com.braintribe.common", "platform-api", "1.2.4"),
				cai("com.braintribe.common", "platform-api", "1.2.6"),
				cpi("com.braintribe.common", "platform-api", "1.2.4", null, null),
				cpi("com.braintribe.common", "platform-api", "1.2.6", null, null),
				cpi("com.braintribe.common", "platform-api", "1.2.5", "any-classifier", "any-type")
			).matchesNone(
				gi("other.group"),
				ai("com.braintribe.common", "codec-api"),
				ai("other.group", "platform-api"),
				cai("com.braintribe.common", "platform-api", "1.2.3"),
				cai("com.braintribe.common", "platform-api", "1.2.7"),
				cpi("com.braintribe.common", "platform-api", "1.2.7", null, null)
			);

		// test classifier and type
		assertThat(qualified("com.braintribe.common", "platform-api", "1.2.6", "sources", "jar")).matchesAll(
				gi("com.braintribe.common"),
				ai("com.braintribe.common", "platform-api"),
				cai("com.braintribe.common", "platform-api", "1.2.6"),
				cpi("com.braintribe.common", "platform-api", "1.2.6", "sources", "jar")
			).matchesNone(
				gi("com.braintribe.commox"),
				ai("com.braintribe.common", "codec-api"),
				cai("com.braintribe.common", "platform-api", "1.2.13"),
				cpi("com.braintribe.commox", "platform-api", "1.2.6", "sources", "jar"),
				cpi("com.braintribe.common", "platform-apx", "1.2.6", "sources", "jar"),
				cpi("com.braintribe.common", "platform-api", "1.2.7", "sources", "jar"),
				cpi("com.braintribe.common", "platform-api", "1.2.6", "sourcex", "jar"),
				cpi("com.braintribe.common", "platform-api", "1.2.6", "sources", "jax")
			);

		// test empty classifier and type
		assertThat(qualified("com.braintribe.common", "platform-api", "1.2.6", "", "")).matchesAll(
				gi("com.braintribe.common"),
				ai("com.braintribe.common", "platform-api"),
				cai("com.braintribe.common", "platform-api", "1.2.6"),
				cpi("com.braintribe.common", "platform-api", "1.2.6", "", ""),
				// null is handled as empty string
				cpi("com.braintribe.common", "platform-api", "1.2.6", null, null)
			).matchesNone(
				cpi("com.braintribe.common", "platform-api", "1.2.6", "sources", null),
				cpi("com.braintribe.common", "platform-api", "1.2.6", null, "jar"),
				cpi("com.braintribe.common", "platform-api", "1.2.6", "sources", "jar")
			);

		// test empty classifier and type (wildcard must match empty string)
		assertThat(qualified("com.braintribe.common", "platform-api", "1.2.6", "*", "*")).matchesAll(
				gi("com.braintribe.common"),
				ai("com.braintribe.common", "platform-api"),
				cai("com.braintribe.common", "platform-api", "1.2.6"),
				cpi("com.braintribe.common", "platform-api", "1.2.6", "", ""),
				// null is handled as empty string
				cpi("com.braintribe.common", "platform-api", "1.2.6", null, null),
				cpi("com.braintribe.common", "platform-api", "1.2.6", "sources", null),
				cpi("com.braintribe.common", "platform-api", "1.2.6", null, "jar"),
				cpi("com.braintribe.common", "platform-api", "1.2.6", "sources", "jar")
			).matchesNone(
				cpi("com.braintribe.commox", "platform-api", "1.2.6", "sources", "jar"),
				cpi("com.braintribe.common", "platform-apx", "1.2.6", "sources", "jar"),
				cpi("com.braintribe.common", "platform-api", "1.2.7", "sources", "jar")
			);
		
		// test regex pattern test
		assertThat(qualified("regex:(abc.*|.*def)", null, null, null, null)).matchesAll(
				gi("abc"),
				gi("xdef"),
				ai("abc", "platform-api"),
				ai("abcd", "platform-api"),
				ai("abcde-something", "platform-api"),
				ai("def", "platform-api"),
				ai("xdef", "platform-api"),
				ai("abcdef", "platform-api")
			).matchesNone(
				gi("ab"),
				gi("abx"),
				ai("abx", "platform-api"),
				ai("dxef", "platform-api")
			);

		// @formatter:on
	}
}
