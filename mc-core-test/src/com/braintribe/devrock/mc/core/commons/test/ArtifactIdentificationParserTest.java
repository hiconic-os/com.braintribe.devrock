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
package com.braintribe.devrock.mc.core.commons.test;


import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * tests for the parsing functions of {@link ArtifactIdentification}, {@link VersionedArtifactIdentification}, {@link CompiledArtifactIdentification}
 * @author pit
 *
 */
public class ArtifactIdentificationParserTest {

	@Test
	public void test() {
		String tv = "a.b.c.d:x-y-z";
		
		ArtifactIdentification ai = ArtifactIdentification.parse( tv);		
		String av = ai.asString();		
		Assert.assertTrue("ArtifactIdentification: expected [" + tv + "], found [" + av + "]", tv.compareTo(av) == 0);
		
		String tv2 = tv + "#1.0.0";
		
		VersionedArtifactIdentification vi = VersionedArtifactIdentification.parse(tv2);
		String vv = vi.asString();
		Assert.assertTrue("VersionedArtifactIdentification: expected [" + tv2 + "], found [" + vv + "]", tv2.compareTo(vv) == 0);
		
		
		CompiledArtifactIdentification ci = CompiledArtifactIdentification.parse(tv2);
		String cv = ci.asString();
		Assert.assertTrue("CompiledArtifactIdentification: expected [" + tv2 + "], found [" + cv + "]", tv2.compareTo(cv) == 0);
			
	}

}
