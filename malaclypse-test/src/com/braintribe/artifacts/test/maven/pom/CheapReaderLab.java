// ============================================================================
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
package com.braintribe.artifacts.test.maven.pom;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.build.artifact.representations.artifact.pom.CheapPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class CheapReaderLab {
	private static File contents = new File( "res/cheapReader");

	private void test(File pom, String name) {			
		try {
			Artifact identifyPom = CheapPomReader.identifyPom(pom);
			String result = identifyPom.getGroupId() + ":" + identifyPom.getArtifactId() + "#" + VersionProcessor.toString(identifyPom.getVersion());
			Assert.assertTrue( "expected name [" + name + "] doesn't match found [" + result + "]", name.equalsIgnoreCase(result));
		} catch (PomReaderException e) {
			Assert.fail( "exception [" + e + "] thrown");
			e.printStackTrace();
		}					
	}
	
	
	@Test
	public void testCheap1() {
		test( new File( contents, "cheap.1.pom"), "foo.bar:XCore#1.0.1");
	}
	
	@Test
	public void testCheap2() {
		test( new File( contents, "cheap.2.pom"), "foo.bar:XParent#1.0.1");
	}


}
