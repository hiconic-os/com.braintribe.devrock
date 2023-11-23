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
