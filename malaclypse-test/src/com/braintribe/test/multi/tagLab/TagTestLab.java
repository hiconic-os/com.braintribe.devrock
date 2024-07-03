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
package com.braintribe.test.multi.tagLab;

import java.io.File;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;


/**
 * tests exclusions on the dependency level
 * @author pit
 *
 */
public class TagTestLab extends AbstractTagLab {
		
	
	private static final String TERMINAL = "com.braintribe.devrock.test.tags:tags-terminal#1.0.1";
	protected static File settings = new File( "res/tagsLab/contents/settings.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	/**
	 *  no rule set 
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
		runTest( TERMINAL, expectedNames, ScopeKind.compile, WalkKind.classpath, false, null);
	}
	
	@Test
	public void testAllOut() {		
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test.tags:none#1.0.1",
				"com.braintribe.devrock.test.tags:classpath#1.0.1",
		};
		runTest( TERMINAL, expectedNames, ScopeKind.compile, WalkKind.classpath, false, "!*");	
	}

	
	@Test
	public void testAllin() {
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test.tags:standard#1.0.1",
				"com.braintribe.devrock.test.tags:one#1.0.1",
				"com.braintribe.devrock.test.tags:one-and-two#1.0.1",
				"com.braintribe.devrock.test.tags:classpath#1.0.1",
		};
		runTest( TERMINAL, expectedNames, ScopeKind.compile, WalkKind.classpath, false, "*");	
	}
	
	
	@Test
	public void testOne() {		
		String[] expectedNames = new String [] {								
				"com.braintribe.devrock.test.tags:one#1.0.1",
				"com.braintribe.devrock.test.tags:one-and-two#1.0.1",
		};
		runTest( TERMINAL, expectedNames, ScopeKind.compile, WalkKind.classpath, false, "one");	
	}
	
	@Test
	public void testOneAndTwo() {
		String[] expectedNames = new String [] {												
				"com.braintribe.devrock.test.tags:one-and-two#1.0.1",				
		};
		runTest( TERMINAL, expectedNames, ScopeKind.compile, WalkKind.classpath, false, "one,two");	
	}
	
	@Test
	public void testOneNotTwo() {		
		String[] expectedNames = new String [] {								
				"com.braintribe.devrock.test.tags:one#1.0.1",			
		};
		runTest( TERMINAL, expectedNames, ScopeKind.compile, WalkKind.classpath, false, "one,!two");	
	}
	@Test
	public void testNeitherTwoNorStandard() {		
		String[] expectedNames = new String [] {								
				"com.braintribe.devrock.test.tags:none#1.0.1",
				"com.braintribe.devrock.test.tags:one#1.0.1",
				"com.braintribe.devrock.test.tags:classpath#1.0.1",
		};
		runTest( TERMINAL, expectedNames, ScopeKind.compile, WalkKind.classpath, false, "!two,!standard");	
	}
}
