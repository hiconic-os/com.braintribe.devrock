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
