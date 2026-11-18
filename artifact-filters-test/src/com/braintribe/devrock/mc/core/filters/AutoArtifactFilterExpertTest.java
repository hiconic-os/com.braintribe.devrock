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
import com.braintribe.devrock.model.repository.filters.AutoArtifactFilter;

public class AutoArtifactFilterExpertTest extends AbstractArtifactFilterExpertTest {
	@Test
	public void unresolvedAutoMatchesAllIdentificationLevels() {
		assertThat(AutoArtifactFilter.T.create()).matchesAll(
				gi("org.example"), ai("org.example", "library"), cai("org.example", "library", "1.2.3"),
				cpi("org.example", "library", "1.2.3", "sources", "jar"));
	}
	@Test
	public void unresolvedAutoUsesNormalBooleanSemantics() {
		assertThat(or(none(), AutoArtifactFilter.T.create())).matchesAll(ai("org.example", "library"));
		assertThat(not(AutoArtifactFilter.T.create())).matchesNone(ai("org.example", "library"));
	}
}
