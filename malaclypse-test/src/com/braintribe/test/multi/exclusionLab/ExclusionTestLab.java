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
