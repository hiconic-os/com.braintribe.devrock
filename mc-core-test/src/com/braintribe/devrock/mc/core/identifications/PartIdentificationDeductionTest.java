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
package com.braintribe.devrock.mc.core.identifications;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;

/**
 * simple test to check the reverse creation of a file into a {@link CompiledPartIdentification}
 * 
 * @author pit
 *
 */
public class PartIdentificationDeductionTest {
	
	private Map<String,Map<String,String>> tests = new HashMap<>();
	{
		Map<String,String> forA = new HashMap<>();
		forA.put("a-1.0.pom", "/:pom");
		forA.put("a-1.0.jar", "/:jar");
		forA.put("a-1.0-sources.jar", "/sources:jar");
		forA.put("a-1.0-asset.man", "/asset:man");
		forA.put("a-1.0-properties.zip", "/properties:zip");
		forA.put("bla", ""); // that will return a <null> value
		
		tests.put("com.braintribe.decrock.test:a#1.0", forA);
	}
	
	@Test
	public void partIdentificationTest() {	
		for (Map.Entry<String, Map<String,String>> entry : tests.entrySet()) {							
			CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse( entry.getKey());
			Map<String,String> namesToPiMap = entry.getValue();
			
			for (Map.Entry<String, String> nameEntry : namesToPiMap.entrySet()) {
				CompiledPartIdentification pi = CompiledPartIdentification.fromFile(cai, nameEntry.getKey());
				if (pi != null) {				
					System.out.println( entry.getKey() + " -> " + pi.asString());
					String expected = cai.asString() + nameEntry.getValue();
					String pis = pi.asString();
					Assert.assertTrue("expected [" + expected + "] yet found [" + pis + "]", expected.equals( pis));
				}
				else {
					System.out.println( entry.getKey() + " -> <unknown>");
				}												
			}
		}		
	}
}
