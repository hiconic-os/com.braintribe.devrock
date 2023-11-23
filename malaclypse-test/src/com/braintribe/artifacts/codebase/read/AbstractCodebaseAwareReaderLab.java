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
package com.braintribe.artifacts.codebase.read;

import java.io.File;
import java.util.UUID;

import org.junit.Assert;

import com.braintribe.artifacts.codebase.AbstractCodebaseAwareLab;
import com.braintribe.artifacts.codebase.CodebaseGenerator;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.CrcValidationLevel;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.panther.SourceRepository;



public abstract class AbstractCodebaseAwareReaderLab extends AbstractCodebaseAwareLab{
	
	
	protected static File targetGrpOneA;
	protected static File targetGrpOneB;
	protected static File targetGrpOneSubOne;
	

	protected static void setupCodebase(String templateStr) {
	
		CodebaseGenerator codebaseGenerator = new CodebaseGenerator();
		codebaseGenerator.transfer(masterCodebase, target, templateStr);
	}
	
	protected static void runbefore(String templateStr) {
		try {			
			runBefore( CrcValidationLevel.ignore);
			SourceRepository sourceRepository = SourceRepository.T.create();			
			sourceRepository.setRepoUrl( target.toURI().toURL().toString());			
			groupingAwareResolverFactory.setSourceRepository(sourceRepository);
			//"${groupId}/${version}/${artifactId}"
			groupingAwareResolverFactory.setCodebaseTemplate( templateStr);
			pomExpertFactory.setDependencyResolverFactory(groupingAwareResolverFactory);
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
	public void testPomResolvingOnGrpOne() {
		//new File( contents, "com.braintribe.grpOne/1.0/A/pom.xml")
		Solution solutionGrpOneA = test( targetGrpOneA);
		assertSolution(solutionGrpOneA, "com.braintribe.grpOne", "A", "1.0.1", "com.braintribe.grpOne:C#[1.0,1.1)");
		//new File( contents, "com.braintribe.grpOne/1.0/B/pom.xml")
		Solution solutionGrpOneB = test( targetGrpOneB);
		assertSolution(solutionGrpOneB, "com.braintribe.grpOne", "B", "1.0.1", "com.braintribe.grpOne:D#[1.0,1.1)");
	}
	
	//@Test
	public void testPomResolvingOnGrpTwo() {
		//new File( contents, "com.braintribe.grpOne.subOne/1.0/A/pom.xml")
		Solution solutionGrpOneA = test( targetGrpOneSubOne);
		assertSolution(solutionGrpOneA, "com.braintribe.grpOne.subOne", "A", "1.0.1", 
				"com.braintribe.grpOne:A#[1.0,1.1)", 
				"com.braintribe.grpOne.subOne:B#[1.0,1.1)"
		);
		
	}

}
