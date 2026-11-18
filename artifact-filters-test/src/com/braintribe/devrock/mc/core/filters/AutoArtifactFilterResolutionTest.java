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
import static com.braintribe.devrock.mc.core.filters.ArtifactFilters.resolveAutoFilter;
import static org.junit.Assert.*;
import org.junit.Test;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.AutoArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ConjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.StandardDevelopmentViewArtifactFilter;

public class AutoArtifactFilterResolutionTest extends AbstractArtifactFilterExpertTest {
	@Test
	public void unspecifiedAndRootAutoUseDerivedFilter() {
		ArtifactFilter derived = groups("indexed");
		assertSame(derived, resolveAutoFilter(null, derived));
		assertSame(derived, resolveAutoFilter(AutoArtifactFilter.T.create(), derived));
		assertNull(resolveAutoFilter(null, null));
	}
	@Test
	public void explicitFiltersAreNotImplicitlyIntersected() {
		assertThat(resolveAutoFilter(all(), groups("indexed"))).matchesAll(ai("third.party", "library"));
		assertThat(resolveAutoFilter(groups("third.party"), groups("indexed")))
				.matchesAll(ai("third.party", "library")).matchesNone(ai("indexed", "library"));
		assertThat(resolveAutoFilter(none(), groups("indexed"))).matchesNone(ai("indexed", "library"));
	}
	@Test
	public void autoCanRestrictOrExtendAnExplicitFilter() {
		ArtifactFilter indexed = groups("indexed");
		assertThat(resolveAutoFilter(and(AutoArtifactFilter.T.create(), qualified("indexed", "allowed", null, null, null)), indexed))
				.matchesAll(ai("indexed", "allowed")).matchesNone(ai("indexed", "other"), ai("third.party", "allowed"));
		assertThat(resolveAutoFilter(or(AutoArtifactFilter.T.create(), groups("third.party")), indexed))
				.matchesAll(ai("indexed", "library"), ai("third.party", "library")).matchesNone(ai("unrelated", "library"));
	}
	@Test
	public void nestedPlaceholdersRespectNegationAndMultipleOccurrences() {
		ArtifactFilter configured = and(or(AutoArtifactFilter.T.create(), groups("extra")), not(AutoArtifactFilter.T.create()));
		assertThat(resolveAutoFilter(configured, groups("indexed"))).matchesAll(ai("extra", "library"))
				.matchesNone(ai("indexed", "library"), ai("unrelated", "library"));
	}
	@Test
	public void autoIsResolvedInsideDevelopmentViewRestriction() {
		StandardDevelopmentViewArtifactFilter configured = standardDevelopmentView(AutoArtifactFilter.T.create(), false);
		StandardDevelopmentViewArtifactFilter resolved = (StandardDevelopmentViewArtifactFilter) resolveAutoFilter(configured, groups("indexed"));
		assertFalse(resolved.getRestrictionFilter() instanceof AutoArtifactFilter);
		assertTrue(configured.getRestrictionFilter() instanceof AutoArtifactFilter);
	}
	@Test
	public void configurationCanBeResolvedAgainWithNewIndexWithoutMutation() {
		AutoArtifactFilter auto = AutoArtifactFilter.T.create();
		ConjunctionArtifactFilter configured = and(auto, all());
		ArtifactFilter first = resolveAutoFilter(configured, groups("first"));
		ArtifactFilter second = resolveAutoFilter(configured, groups("second"));
		assertSame(auto, configured.getOperands().get(0));
		assertThat(first).matchesAll(ai("first", "library")).matchesNone(ai("second", "library"));
		assertThat(second).matchesAll(ai("second", "library")).matchesNone(ai("first", "library"));
	}
	@Test
	public void missingDerivedFilterLeavesAutoUnrestricted() {
		assertThat(resolveAutoFilter(AutoArtifactFilter.T.create(), null)).matchesAll(ai("anything", "library"));
		assertThat(resolveAutoFilter(and(AutoArtifactFilter.T.create(), groups("explicit")), null))
				.matchesAll(ai("explicit", "library")).matchesNone(ai("other", "library"));
	}
	@Test
	public void sharedAutoReferencesAreAllReplaced() {
		AutoArtifactFilter auto = AutoArtifactFilter.T.create();
		assertThat(resolveAutoFilter(and(auto, auto), groups("indexed")))
				.matchesAll(ai("indexed", "library")).matchesNone(ai("other", "library"));
	}
}
