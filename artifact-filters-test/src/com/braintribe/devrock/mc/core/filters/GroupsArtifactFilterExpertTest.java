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
 * Provides tests for {@link GroupsArtifactFilterExpert}.
 *
 * @author michael.lafite
 */
public class GroupsArtifactFilterExpertTest extends AbstractArtifactFilterExpertTest {

	@Test
	public void test() {
		// @formatter:off

		// test no group
		assertThat(groups()).matchesNone(
				gi("com.braintribe"),
				ai("com.braintribe", "my-artifact"),
				cai("com.braintribe.common2", "my-artifact", "1.2.3"),
				cpi("com.braintribe.common2", "my-artifact", "1.2.3", null, null)
			);

		// test single group
		assertThat(groups("com.braintribe.common")).matchesAll(
				gi("com.braintribe.common"),
				ai("com.braintribe.common", "my-artifact"),
				cai("com.braintribe.common", "my-artifact", "1.2.3"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", null, null),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", "sources", null),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", null, "jar"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", "sources", "jar")
			).matchesNone(
				gi("com.braintribe"),
				gi("com.braintribe.common2"),
				gi("com.braintribe.common.subgroup"),
				ai("com.braintribe", "my-artifact"),
				ai("com.braintribe.other", "my-artifact"),
				ai("com.braintribe.common2", "my-artifact"),
				ai("com.braintribe.common.subgroup", "my-artifact"),
				cai("com.braintribe.common2", "my-artifact", "1.2.3"),
				cpi("com.braintribe.common2", "my-artifact", "1.2.3", null, null),
				cpi("com.braintribe.common2", "my-artifact", "1.2.3", "sources", null),
				cpi("com.braintribe.common2", "my-artifact", "1.2.3", null, "jar"),
				cpi("com.braintribe.common2", "my-artifact", "1.2.3", "sources", "jar")
			);

		// test multiple groups
		assertThat(groups("com.braintribe","com.braintribe.common", "tribefire.cortex")).matchesAll(
				gi("com.braintribe"),
				gi("com.braintribe.common"),	
				ai("com.braintribe", "my-artifact"),
				cai("com.braintribe.common", "my-artifact", "1.2.3"),
				cpi("tribefire.cortex", "other-artifact", "1.2.3", null, null),
				cpi("tribefire.cortex", "other-artifact", "1.2.3", "sources", null),
				cpi("tribefire.cortex", "other-artifact", "1.2.3", null, "jar"),
				cpi("tribefire.cortex", "other-artifact", "1.2.3", "sources", "jar")
			).matchesNone(
				gi("com.braintribex"),
				ai("com.braintribex", "my-artifact"),
				ai("com.braintribe.common.subgroup", "my-artifact")
			);

		// @formatter:on
	}
}
