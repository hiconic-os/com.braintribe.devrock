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
package com.braintribe.artifacts.test.version;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;


public class VersionAmbiguityTest {

	@Test
	public void testKamran() {
		String versionAsString1="3.2.4-RELEASE";
		String versionAsString2="3.2.4.RELEASE";
		
		try {
			Version version1 = VersionProcessor.createFromString(versionAsString1);
			Version version2 = VersionProcessor.createFromString(versionAsString2);
			
			System.out.println( "direct match : " + VersionProcessor.matches(version1, version2));
			
		} catch (VersionProcessingException e) {
			Assert.fail( "Exception [" + e + "] thrown");
		}
				
	}
	
	@Test
	public void testRainer() {
		String versionAsString1="1.7.0";
		String versionAsString2="1.7-dev";
		
		try {
			Version version1 = VersionProcessor.createFromString(versionAsString1);
			Version version2 = VersionProcessor.createFromString(versionAsString2);
			
			System.out.println( "direct match : " + VersionProcessor.matches(version1, version2));
			
		} catch (VersionProcessingException e) {
			Assert.fail( "Exception [" + e + "] thrown");
		}
		
		
	}

}
