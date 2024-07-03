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
package com.braintribe.test.multi.subtreeExclusion;

import java.io.File;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Solution;


public class SubTreeExclusionLab extends AbstractSubtreeExclusionLab {
		
	
	protected static File settings = new File( "res/subtreeExclusion/contents/settings.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testSubTreeExclusion() {
		String[] expectedNames = new String [] {
				"com.braintribe.test.dependencies.subtreeexclusiontest:B#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:C#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:D#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:E#1.1",
				"com.braintribe.test.dependencies.subtreeexclusiontest:F#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:G#1.1",
				"com.braintribe.test.dependencies.subtreeexclusiontest:H#1.0",		
				"com.braintribe.test.dependencies.subtreeexclusiontest:T#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:U#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:V#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:X#1.1",
		};
		
		runTest( "com.braintribe.test.dependencies.subtreeexclusiontest:A#1.0", expectedNames);
	}
	
	@Test
	public void testSubTreeExclusionMerge() {
		
		String[] expectedNames = new String [] {
				"com.braintribe.test.dependencies.subtreeexclusiontest:B#1.1",
				"com.braintribe.test.dependencies.subtreeexclusiontest:C#1.1",
				"com.braintribe.test.dependencies.subtreeexclusiontest:D#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:E#1.1",
				"com.braintribe.test.dependencies.subtreeexclusiontest:F#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:G#1.1",
				"com.braintribe.test.dependencies.subtreeexclusiontest:H#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:N#1.5",
				"com.braintribe.test.dependencies.subtreeexclusiontest:O#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:Q#1.9",
				"com.braintribe.test.dependencies.subtreeexclusiontest:R#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:T#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:U#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:V#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:X#1.1",
		};
		
		runTest( "com.braintribe.test.dependencies.subtreeexclusiontest:A#1.1", expectedNames);
	}
	
	@Test
	public void testSubTreeExclusionSimpleA() {
		
		String[] expectedNames = new String [] {
				"com.braintribe.test.dependencies.subtreeexclusiontest:D#1.0",			
				"com.braintribe.test.dependencies.subtreeexclusiontest:E#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:F#1.1",
				"com.braintribe.test.dependencies.subtreeexclusiontest:G#1.1",
				"com.braintribe.test.dependencies.subtreeexclusiontest:H#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:K#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:L#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:M#1.0",
		};
	
		runTest( "com.braintribe.test.dependencies.subtreeexclusiontest:X#1.0", expectedNames);
	}
	
	@Test
	public void testSubTreeExclusionSimpleB() {
		
		String[] expectedNames = new String [] {
				"com.braintribe.test.dependencies.subtreeexclusiontest:D#1.0",			
				"com.braintribe.test.dependencies.subtreeexclusiontest:E#1.1",
				"com.braintribe.test.dependencies.subtreeexclusiontest:F#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:G#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:H#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:T#1.0",
				"com.braintribe.test.dependencies.subtreeexclusiontest:U#1.0",					
				"com.braintribe.test.dependencies.subtreeexclusiontest:V#1.0",
		};
		
		
		runTest( "com.braintribe.test.dependencies.subtreeexclusiontest:X#1.1", expectedNames);
	}

	
}
