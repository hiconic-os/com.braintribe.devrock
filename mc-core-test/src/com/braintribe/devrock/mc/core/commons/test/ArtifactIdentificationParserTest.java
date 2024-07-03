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
