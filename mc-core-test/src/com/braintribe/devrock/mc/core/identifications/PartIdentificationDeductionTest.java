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
