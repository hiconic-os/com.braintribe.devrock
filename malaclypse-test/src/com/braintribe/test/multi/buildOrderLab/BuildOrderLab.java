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
package com.braintribe.test.multi.buildOrderLab;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;



/**
 
 * 
 * @author pit
 *
 */
public class BuildOrderLab extends AbstractBuildOrderLab {
		
	
	protected static File settings = new File( "res/buildOrderTest/contents/settings.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testBuildOrder() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.buildOrderTest:D#1.0",
				"com.braintribe.test.dependencies.buildOrderTest:C#1.0",
				"com.braintribe.test.dependencies.buildOrderTest:B#1.0",
				"com.braintribe.test.dependencies.buildOrderTest:A#1.0",
		};
		
		// don't sort the output, keep it exactly as produced.. 
		Collection<Solution> collection = runTest( "com.braintribe.test.dependencies.buildOrderTest:BuildOrderTestTerminal#1.0", expectedNames, ScopeKind.compile, WalkKind.buildOrder, false);
		Solution [] solutions = collection.toArray( new Solution[0]);
		if (collection.size() == expectedNames.length)  {
			for (int i = 0; i < expectedNames.length; i++) {
				String expected = expectedNames[i];
				String found = NameParser.buildName( solutions[i]);
				Assert.assertTrue( "expected [" + expected + "] at position [" + i + "], but found [" + found + "]", expected.equalsIgnoreCase(found));	
			}
		}
		else {
			Assert.fail("result's size doesn't match expectations");
		}
	}
	
	
	

	
}
