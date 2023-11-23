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
package com.braintribe.test.multi.classifierLab;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;


/**
 
 * 
 * @author pit
 *
 */
public class ClassifierTestLab extends AbstractClassifierLab {
		
	
	protected static File settings = new File( "res/classifierTest/contents/settings.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testClassifierOne() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.classifier:A#1.0",								
		};
		
		Map<Solution, List<PartTuple>> runTest = runTest( "com.braintribe.test.dependencies.classifier:ClassifierTestTerminal#1.0", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
		
		//
		PartTuple expectedTuple = PartTupleProcessor.fromString("One", "jar");
		validate(runTest, expectedTuple);
		
	}


	
	@Test
	public void testClassifierTwo() {
		String[] expectedNames = new String [] {					
				"com.braintribe.test.dependencies.classifier:A#1.0",								
		};
		
		Map<Solution, List<PartTuple>> runTest = runTest( "com.braintribe.test.dependencies.classifier:ClassifierTestTerminal#1.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	
		// 
		PartTuple expectedTuple = PartTupleProcessor.fromString("Two", "jar");
		validate(runTest, expectedTuple);
	}
	
	private void validate(Map<Solution, List<PartTuple>> runTest, PartTuple expectedTuple) {
		for (Entry<Solution, List<PartTuple>> entry : runTest.entrySet()) {
			boolean foundTuple = false;
			for (PartTuple tuple : entry.getValue()) {
				if (PartTupleProcessor.equals(expectedTuple, tuple)) {
					foundTuple = true;
					break;
				}
			}
			Assert.assertTrue( "expected a partuple [" + PartTupleProcessor.toString(expectedTuple)+ "] but not found in list", foundTuple);
			boolean foundPart = false;
			for (Part part : entry.getKey().getParts()) {
				if (
						PartTupleProcessor.equals(expectedTuple, part.getType()) &&
						part.getLocation() != null &&
						new File( part.getLocation()).exists()
						) {					
					foundPart = true;
				}
			}
			Assert.assertTrue( "expected retrieved part with [" + PartTupleProcessor.toString(expectedTuple) + "] but not found in list", foundPart);
		}
	}
	

	
}
