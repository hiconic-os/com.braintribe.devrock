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
