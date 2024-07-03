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
