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
