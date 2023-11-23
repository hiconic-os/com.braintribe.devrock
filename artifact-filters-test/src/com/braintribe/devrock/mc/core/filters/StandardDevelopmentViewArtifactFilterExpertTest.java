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

import com.braintribe.devrock.model.repository.filters.ArtifactFilter;

/**
 * Provides tests for {@link StandardDevelopmentViewArtifactFilterExpert}.
 *
 * @author michael.lafite
 */
public class StandardDevelopmentViewArtifactFilterExpertTest extends AbstractArtifactFilterExpertTest {

	@Test
	public void test() {
		// @formatter:off

		ArtifactFilter restrictionFilter = locks("com.braintribe.common:my-artifact#1.2.3", "com.braintribe.common:my-artifact#1.2.4", "tribefire.cortex:other-artifact#2.0.1");
		
		assertThat(standardDevelopmentView(restrictionFilter, false)).matchesAll(
				gi("com.braintribe.common"),
				gi("tribefire.cortex"),
				gi("any.group"),
				ai("com.braintribe.common", "my-artifact"),
				ai("tribefire.cortex", "other-artifact"),
				ai("any.group", "any-artifact"),
				cai("com.braintribe.common", "my-artifact", "1.2.3"),
				cai("com.braintribe.common", "my-artifact", "1.2.4"),
				cai("tribefire.cortex", "other-artifact", "2.0.1"),
				cai("any.group", "any-artifact", "1.2.3"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", "sources", "jar"),
				cpi("any.group", "any-artifact", "1.2.3", "any-classifier", "any-type")
			).matchesNone(
				ai("tribefire.cortex", "any-artifact"), // unknown artifacts from matched groups are not allowed
				cai("com.braintribe.common", "my-artifact", "1.2.5")
			);
		
		// identifications below are a copy/paste of ones above except for the lines with comments
		assertThat(standardDevelopmentView(restrictionFilter, true)).matchesAll(
				gi("com.braintribe.common"),
				gi("tribefire.cortex"),
				gi("any.group"),
				ai("com.braintribe.common", "my-artifact"),
				ai("tribefire.cortex", "other-artifact"),
				ai("tribefire.cortex", "any-artifact"), // unknown artifacts from matched groups are allowed
				ai("any.group", "any-artifact"),
				cai("com.braintribe.common", "my-artifact", "1.2.3"),
				cai("com.braintribe.common", "my-artifact", "1.2.4"),
				cai("tribefire.cortex", "other-artifact", "2.0.1"),
				cai("any.group", "any-artifact", "1.2.3"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", "sources", "jar"),
				cpi("any.group", "any-artifact", "1.2.3", "any-classifier", "any-type")
			).matchesNone(
				cai("com.braintribe.common", "my-artifact", "1.2.5")
			);

		// @formatter:on
	}
}
