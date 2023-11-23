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
