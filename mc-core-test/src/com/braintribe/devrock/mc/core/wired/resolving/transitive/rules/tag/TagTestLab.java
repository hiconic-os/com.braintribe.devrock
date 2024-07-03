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
