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
package com.braintribe.test.multi.exclusionLab;

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
public class ExclusionTestLab extends AbstractExclusionLab {
		
	
	protected static File settings = new File( "res/subtreeExclusion/contents/settings.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	/**
	 *  tests qualified exclusions, i.e. with declared groupId and artifactId
	 */
	@Test
	public void testExplicitExclusion() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.exclusionTest:A#1.0",
				"com.braintribe.test.dependencies.exclusionTest:B#1.0",		
				"com.braintribe.test.dependencies.exclusionTest:D#1.0",		
		};
		
		runTest( "com.braintribe.test.dependencies.exclusionTest:ExclusionTestTerminal#1.0", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	
	
	/**
	 * tests wild card exclusions, i.e. with * for groupdId and artifactid
	 */
	@Test
	public void testWildcardExclusion() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.exclusionTest:A#1.0",
				"com.braintribe.test.dependencies.exclusionTest:B#1.0",			
		};
		
		runTest( "com.braintribe.test.dependencies.exclusionTest:ExclusionTestTerminal#1.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	/**
	 * tests empty exclusions, i.e. empty exclusion element
	 */
	@Test
	public void testMinimalExclusion() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.exclusionTest:A#1.0",
				"com.braintribe.test.dependencies.exclusionTest:B#1.0",			
		};
		
		runTest( "com.braintribe.test.dependencies.exclusionTest:ExclusionTestTerminal#1.2", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
}
