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
package com.braintribe.test.multi.classifierLookupLab;

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
 * tests the lookup behavior when the pom reader is accessing the dependency management section in the parent chain:
 * all GROUP ID, ARTIFACT ID and CLASSIFIER of a dependency is used to match an entry in the section.
 * 
 * @author pit
 *
 */
public class ClassifierLookupTestLab extends AbstractClassifierLookupLab {
		
	
	protected static File settings = new File( "res/classifierTest/contents/settings.xml");
	private static final String GROUP = "com.braintribe.devrock.test.classifier.lookup";
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testClassifierLookup_1() {
		String[] expectedNames = new String [] {					
				GROUP + ":classifier-a#1.0",								
		};
		
		Map<Solution, List<PartTuple>> runTest = runTest( GROUP + ":classifier-terminal-a#1.0.1-pc", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
		
		//
		PartTuple expectedTuple = PartTupleProcessor.createJarPartTuple();
		validate(runTest, expectedTuple);
		
	}


	
	@Test
	public void testClassifierLookup_2() {
		String[] expectedNames = new String [] {					
				GROUP + ":classifier-a#2.0",								
		};
		
		Map<Solution, List<PartTuple>> runTest = runTest( GROUP + ":classifier-terminal-b#1.0.1-pc", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	
		// 
		PartTuple expectedTuple = PartTupleProcessor.fromString("redirect", "jar");
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
