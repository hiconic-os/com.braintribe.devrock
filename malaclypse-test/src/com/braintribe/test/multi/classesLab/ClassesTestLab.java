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
package com.braintribe.test.multi.classesLab;

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
public class ClassesTestLab extends AbstractClassesLab {
		
	
	protected static File settings = new File( "res/classesTest/contents/settings.xml");
	
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
				"com.braintribe.devrock.test.classes:a#1.0",								
		};
		
		Map<Solution, List<PartTuple>> runTest = runTest( "com.braintribe.devrock.test.classes:classifier-test-terminal#1.0", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
		
		//
		PartTuple expectedTuple = PartTupleProcessor.fromString("classes", "jar");
		validate(runTest, expectedTuple);
		
	}


	
	@Test
	public void testClassifierTwo() {
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test.classes:a#1.0",								
		};
		
		Map<Solution, List<PartTuple>> runTest = runTest( "com.braintribe.devrock.test.classes:classifier-test-terminal#1.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	
		// 
		PartTuple expectedTuple = PartTupleProcessor.fromString("classes", "jar");
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
