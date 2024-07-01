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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.model.artifact.Solution;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class PomReaderIssues extends AbstractPomReaderLab{
	
	private File contents = new File("res/issues/pomreader");
	
	@BeforeClass
	public static void beforeClass() {
		runbefore(null, null);
	}

	//@Test
	public void testVersionResolving() {
		boolean exceptionThrown = false;
		try {
			Solution readPom = readPom( new File( contents, "maven.pom.xml"));
			System.out.println(NameParser.buildName(readPom));
		} catch (Exception e) {
			exceptionThrown = true;
		}
		Assert.assertTrue( "expected exception not thrown", exceptionThrown);
	}
	
	@Test
	public void testDoesNotExist() {
		Solution readPom = readPom( new File( contents, "doesnotexist.pom.xml"));
		System.out.println(NameParser.buildName(readPom));
	}
	
	@Test
	public void testVarDoesNotExist() {
		boolean exceptionThrown = false;
		try {
			Solution readPom = readPom( new File( contents, "varDoesnotexist.pom.xml"));
			System.out.println(NameParser.buildName(readPom));
		} catch (Exception e) {
			exceptionThrown = true;
		}
		Assert.assertTrue( "expected exception not thrown", exceptionThrown);
	}
	
	//@Test
	public void testPropertyResolvingViaParent() {
		Solution readPom = readPom( new File( contents, "pomResolutionPerReflection.parent.xml"));
		System.out.println(NameParser.buildName(readPom));
	}
	
	//@Test
	public void testGoogleStorageCloud() {
		Solution readPom = readPom( new File( contents, "google-cloud-storage-deployable-experts-1.0.2.pom"));
		System.out.println(NameParser.buildName(readPom));
	}
	
	
	@Test
	public void testGwtDev() {
		Solution readPom = readPom( new File( contents, "gwt-dev-2.8.2.pom"));
		System.out.println(NameParser.buildName(readPom));
	}
}
