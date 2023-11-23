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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.rules.tag;

import java.io.File;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;


/**
 * test the tag rule matcher directly 
 * @author pit
 *
 */
// TODO : once filters are properly inserted into TDR/CPR the test can succeed
@Category( KnownIssue.class)
public class TagTestLab extends AbstractTagLab {
		
	
	private static final String TERMINAL = "com.braintribe.devrock.test.tags:tags-terminal#1.0.1";
	protected static File settings = new File( "res/tagsLab/contents/settings.xml");
	
	/**
	 *  no rule set -> all
	 */
	@Test
	public void testDefault() {		
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test.tags:none#1.0.1",		
				"com.braintribe.devrock.test.tags:classpath#1.0.1",
				"com.braintribe.devrock.test.tags:standard#1.0.1",
				"com.braintribe.devrock.test.tags:one#1.0.1",
				"com.braintribe.devrock.test.tags:one-and-two#1.0.1",								
		};
		runTestOnCpr( TERMINAL, expectedNames, null, true);
	}
	
	@Test
	public void testAllOut() {		
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test.tags:none#1.0.1",
				"com.braintribe.devrock.test.tags:classpath#1.0.1",
		};
		runTestOnCpr( TERMINAL, expectedNames, "!*", true);	
	}

	
	/**
	 * any tag -> only those WITH a tag, none WITHOUT tag
	 */
	@Test
	public void testAllIn() {
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test.tags:standard#1.0.1",
				"com.braintribe.devrock.test.tags:one#1.0.1",
				"com.braintribe.devrock.test.tags:one-and-two#1.0.1",
				"com.braintribe.devrock.test.tags:classpath#1.0.1",
		};
		runTestOnCpr( TERMINAL, expectedNames, "*", true);	
	}
	
	
	@Test
	public void testOne() {		
		String[] expectedNames = new String [] {								
				"com.braintribe.devrock.test.tags:one#1.0.1",
				"com.braintribe.devrock.test.tags:one-and-two#1.0.1",
		};
		runTestOnCpr( TERMINAL, expectedNames, "one", true);	
	}
	
	@Test
	public void testOneAndTwo() {
		String[] expectedNames = new String [] {												
				"com.braintribe.devrock.test.tags:one-and-two#1.0.1",				
		};
		runTestOnCpr( TERMINAL, expectedNames, "one,two", true);	
	}
	
	@Test
	public void testOneNotTwo() {		
		String[] expectedNames = new String [] {								
				"com.braintribe.devrock.test.tags:one#1.0.1",			
		};
		runTestOnCpr( TERMINAL, expectedNames, "one,!two", true);	
	}
	
	@Test
	public void testNeitherTwoNorStandard() {		
		String[] expectedNames = new String [] {								
				"com.braintribe.devrock.test.tags:none#1.0.1",
				"com.braintribe.devrock.test.tags:one#1.0.1",
				"com.braintribe.devrock.test.tags:classpath#1.0.1",
		};
		runTestOnCpr( TERMINAL, expectedNames, "!two,!standard", true);	
	}

	@Test
	public void testTransitivity() {
		String[] expectedNames = new String [] {								
				"com.braintribe.devrock.test:b#1.0.1",
				"com.braintribe.devrock.test:c#1.0.1",
		};
		runTestOnCpr( "com.braintribe.devrock.test:t#1.0.1", expectedNames, "serverdeps", true);	
	}
	
}
