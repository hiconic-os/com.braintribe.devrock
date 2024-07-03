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
package com.braintribe.test.nameParsing;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.testing.category.SpecialEnvironment;

@Category(SpecialEnvironment.class)
public class DependencyNameParsing{

	
	private String testAutoRangify(String input, String expected) {
	
		Dependency dependency = NameParser.parseCondensedDependencyNameAndAutoRangify( input);
		String output = NameParser.buildName(dependency);
		Assert.assertTrue("ouput [" + output + "] doesn't match expection [" + expected + "]", expected.equalsIgnoreCase(output));
		return output;
	}
	
	@Test
	public void testSimple0() {
		String out = testAutoRangify( "com.braintribe.test:Test#1.0", "com.braintribe.test:Test#[1.0,1.1)");
		System.out.println( out);
	}
	@Test
	public void testSimple1() {
		String out = testAutoRangify( "com.braintribe.test:Test#1.1", "com.braintribe.test:Test#[1.1,1.2)");
		System.out.println( out);
	}

	@Test
	public void testAlreadyRanged() {
		String out = testAutoRangify( "com.braintribe.test:Test#[1.0,2.0]", "com.braintribe.test:Test#[1.0,2.0]");
		System.out.println( out);
	}
	
	@Test
	public void testHotfix() {
		String out = testAutoRangify( "com.braintribe.test:Test#1.0.1", "com.braintribe.test:Test#[1.0,1.1)");
		System.out.println( out);
	}
	
	@Test 
	public void testClassifier() {
		String out = testAutoRangify( "com.braintribe.test:Test#1.0|RELEASE", "com.braintribe.test:Test#[1.0,1.1)|RELEASE");
		System.out.println( out);
	}

	@Test 
	public void testBuild() {
		String out = testAutoRangify( "com.braintribe.test:Test#1.0-RELEASE", "com.braintribe.test:Test#[1.0,1.1)");
		System.out.println( out);
	}

}
