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
package com.braintribe.artifacts.codebase.read;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;

import com.braintribe.artifacts.codebase.AbstractCodebaseAwareLab;
import com.braintribe.artifacts.codebase.CodebaseGenerator;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.CrcValidationLevel;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.testing.category.SpecialEnvironment;



public abstract class AbstractLocationAgnosticReaderLab extends AbstractCodebaseAwareLab implements SpecialEnvironment{
	protected static File contents = new File("res/grouping");
	protected static File target;// = new File( contents, "grouping.flattened");
	protected static File targetGrpOneA;
	protected static File targetGrpOneB;
	protected static File targetGrpOneSubOneA;
	
	protected static void setupCodebase(String templateStr) {
		CodebaseGenerator codebaseGenerator = new CodebaseGenerator();
		codebaseGenerator.transfer(masterCodebase, target, templateStr);
	}
	protected static void runbefore() {
		try {
			runBefore( CrcValidationLevel.ignore);
			SourceRepository sourceRepository = SourceRepository.T.create();
			sourceRepository.setRepoUrl( target.toURI().toURL().toString());
			List<SourceRepository> sourceRepositories = Collections.singletonList( sourceRepository);
			agnosticResolverFactory.setSourceRepositories(sourceRepositories);
			pomExpertFactory.setDependencyResolverFactory(agnosticResolverFactory);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");
		}  
	}
	
	private Solution test(File file) {
		
		ArtifactPomReader reader = pomExpertFactory.getReader();
		
		try {
			Solution solution = reader.readPom( UUID.randomUUID().toString(), file);
			return solution;
		} catch (PomReaderException e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");
		}
		return null;
	}
	

	
	//@Test
	public void testGrpOne() {
		//new File( target, "com.braintribe.grpOne/1.0/A/pom.xml")
		Solution solutionGrpOneA = test( targetGrpOneA);
		assertSolution(solutionGrpOneA, "com.braintribe.grpOne", "A", "1.0.1", "com.braintribe.grpOne:C#[1.0,1.1)");
		// new File( target, "com.braintribe.grpOne/1.0/B/pom.xml")
		Solution solutionGrpOneB = test( targetGrpOneB);
		assertSolution(solutionGrpOneB, "com.braintribe.grpOne", "B", "1.0.1", "com.braintribe.grpOne:D#[1.0,1.1)");
	}
	
	//@Test
	public void testGrpTwo() {
		//new File( target, "com.braintribe.grpOne.subOne/1.0/A/pom.xml")
		Solution solutionGrpOneA = test( targetGrpOneSubOneA);
		assertSolution(solutionGrpOneA, "com.braintribe.grpOne.subOne", "A", "1.0.1", 
				"com.braintribe.grpOne:A#[1.0,1.1)", 
				"com.braintribe.grpOne.subOne:B#[1.0,1.1)"
		);
		
	}

}
