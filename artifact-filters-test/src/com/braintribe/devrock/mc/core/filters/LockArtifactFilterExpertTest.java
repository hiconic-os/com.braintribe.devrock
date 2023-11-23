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

import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * Provides tests for {@link LockArtifactFilterExpert}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class LockArtifactFilterExpertTest extends AbstractArtifactFilterExpertTest {

	@Test
	public void test() {
		// @formatter:off

		// test no lock
		assertThat(locks()).matchesNone(
				gi("com.braintribe"),
				ai("com.braintribe", "my-artifact"),
				cai("com.braintribe.common2", "my-artifact", "1.2.3"),
				cpi("com.braintribe.common2", "my-artifact", "1.2.3", null, null)
			);

		// test single lock
		assertThat(locks("com.braintribe.common:my-artifact#1.2.3")).matchesAll(
				gi("com.braintribe.common"),
				ai("com.braintribe.common", "my-artifact"),
				cai("com.braintribe.common", "my-artifact", "1.2.3"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", null, null),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", "sources", null),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", null, "jar"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", "sources", "jar")
			).matchesNone(
				gi("com.braintribe.common2"),
				ai("com.braintribe.common2", "my-artifact"),
				ai("com.braintribe.common", "my-artifact2"),
				cai("com.braintribe.common2", "my-artifact", "1.2.3"),
				cai("com.braintribe.common", "my-artifact2", "1.2.3"),
				cai("com.braintribe.common", "my-artifact", "1.2.2"),
				cai("com.braintribe.common", "my-artifact", "1.2.4"),
				cpi("com.braintribe.common", "my-artifact", "1.2.4", null, null)
		);

		// test multiple locks
		assertThat(locks("com.braintribe.common:my-artifact#1.2.3", "com.braintribe.common:my-artifact#1.2.4", "tribefire.cortex:other-artifact#2.0.1", "net.sf.jtidy:jtidy#r938")).matchesAll(
				gi("com.braintribe.common"),
				ai("com.braintribe.common", "my-artifact"),
				cai("com.braintribe.common", "my-artifact", "1.2.3"),
				cpi("com.braintribe.common", "my-artifact", "1.2.3", null, null),
				cpi("com.braintribe.common", "my-artifact", "1.2.4", null, null),				
				gi("tribefire.cortex"),
				ai("tribefire.cortex", "other-artifact"),
				cai("tribefire.cortex", "other-artifact", "2.0.1"),
				cpi("tribefire.cortex", "other-artifact", "2.0.1", "sources", "jar"),				
				gi("net.sf.jtidy"),
				ai("net.sf.jtidy", "jtidy"),
				cai("net.sf.jtidy", "jtidy", "r938"),
				cpi("net.sf.jtidy", "jtidy", "r938", null, null),
				cpi("net.sf.jtidy", "jtidy", "r938", "javadoc", "jar")		
			).matchesNone(
				gi("tribefire.cortex2"),
				ai("com.braintribe.common", "my-artifact2"),
				cai("com.braintribe.common", "my-artifact", "1.2.5"),
				cpi("tribefire.cortex2", "other-artifact", "2.0.1", "sources", "jar"),	
				gi("nt.sf"),
				gi("nt.sf.jtidy.subgroup"),
				cai("net.sf.jtidy", "jtidy2", "r938"),
				cai("net.sf.jtidy", "jtidy", "938"),
				cai("net.sf.jtidy", "jtidy", "r939")
			);

		// @formatter:on

		// make sure that matches-methods also work correctly with identification subtypes
		ArtifactFilterExpert filterExpert = ArtifactFilters.forDenotation(locks("com.braintribe.common:my-artifact#1.2.3"));
		assertThat(filterExpert.matches(ai("com.braintribe.common", "my-artifact"))).isTrue();
		assertThat(filterExpert.matches((ArtifactIdentification) cai("com.braintribe.common", "my-artifact", "1.2.3"))).isTrue();
		assertThat(filterExpert.matches((ArtifactIdentification) cpi("com.braintribe.common", "my-artifact", "1.2.3", "sources", "jar"))).isTrue();
		assertThat(filterExpert.matches((CompiledArtifactIdentification) cpi("com.braintribe.common", "my-artifact", "1.2.3", "sources", "jar"))).isTrue();
	}
}
