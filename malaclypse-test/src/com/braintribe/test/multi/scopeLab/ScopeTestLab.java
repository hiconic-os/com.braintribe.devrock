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
package com.braintribe.test.multi.scopeLab;

import java.io.File;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.DependencyScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.ScopeTreatement;


/**
 * ScopeTestTerminal
 * 	A
 * 		C (provided) - should only appear if A is the terminal and compile build is active 
 * 	B
 * 		D (optional) 
 * 	P (provided)
 * 	T (test)
 * 
 * @author pit
 *
 */
public class ScopeTestLab extends AbstractScopeLab {
		
	
	protected static File settings = new File( "res/scopeTest/contents/settings.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testCompileOptionalIncluded() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.scopeTest:A#1.0",
				"com.braintribe.test.dependencies.scopeTest:B#1.0",					
				"com.braintribe.test.dependencies.scopeTest:D#1.0",				
				"com.braintribe.test.dependencies.scopeTest:P#1.0",
				
		};
		
		runTest( "com.braintribe.test.dependencies.scopeTest:ScopeTestTerminal#1.0", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	@Test
	public void testCompileOnA_ProvidedTest() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.scopeTest:C#1.0",
		};
		
		runTest( "com.braintribe.test.dependencies.scopeTest:A#1.0", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	@Test
	public void testLaunchOptionalIncluded() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.scopeTest:A#1.0",
				"com.braintribe.test.dependencies.scopeTest:B#1.0",
				"com.braintribe.test.dependencies.scopeTest:D#1.0",	
				
		};
		
		runTest( "com.braintribe.test.dependencies.scopeTest:ScopeTestTerminal#1.0", expectedNames, ScopeKind.launch, WalkKind.classpath, false);
	}
	
	@Test
	public void testCompileOptionalExcluded() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.scopeTest:A#1.0",
				"com.braintribe.test.dependencies.scopeTest:B#1.0",	
				"com.braintribe.test.dependencies.scopeTest:P#1.0",	
		};
		
		runTest( "com.braintribe.test.dependencies.scopeTest:ScopeTestTerminal#1.0", expectedNames, ScopeKind.compile, WalkKind.classpath, true);
	}
	
	@Test
	public void testLaunchOptionalExcluded() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.scopeTest:A#1.0",
				"com.braintribe.test.dependencies.scopeTest:B#1.0",			
		};
		
		runTest( "com.braintribe.test.dependencies.scopeTest:ScopeTestTerminal#1.0", expectedNames, ScopeKind.launch, WalkKind.classpath, true);
	}
	
	@Test
	public void testCompileOptionalExcludedPlusTest() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.scopeTest:A#1.0",
				"com.braintribe.test.dependencies.scopeTest:B#1.0",	
				"com.braintribe.test.dependencies.scopeTest:P#1.0",
				"com.braintribe.test.dependencies.scopeTest:T#1.0",
		};
		DependencyScope testScope = DependencyScope.T.create();
		testScope.setName("test");
		testScope.setScopeTreatement(ScopeTreatement.INCLUDE);
		
		runTest( "com.braintribe.test.dependencies.scopeTest:ScopeTestTerminal#1.0", expectedNames, ScopeKind.compile, WalkKind.classpath, true, testScope);
	}
	

	
}
