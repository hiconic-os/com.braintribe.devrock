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
import org.junit.BeforeClass;

import com.braintribe.artifacts.codebase.AbstractCodebaseAwareLab;
import com.braintribe.artifacts.codebase.CodebaseGenerator;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.CrcValidationLevel;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.panther.SourceRepository;

public class GrpFlatCommonLab extends AbstractCodebaseAwareLab {
	protected static File target = new File( contents, "grouping.flattened");
	
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
			groupingAwareResolverFactory.setCodebaseTemplate( templateStr);
			pomExpertFactory.setDependencyResolverFactory(groupingAwareResolverFactory);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");
		}  
	}
	
	@BeforeClass
	public static void runBefore() {		
		runbefore(FLATTENED_GROUP);
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
	public void testLogging() {
		File logging = new File( target, "com.braintribe.common/1.0/Logging/pom.xml"); 
		Solution solution = test( logging);
		System.out.println( solution);
	}
	//@Test
	public void testLogginNdc() {
		File logging = new File( target, "com.braintribe.common/1.0/LoggingNdc/pom.xml");
		Solution solution = test( logging);
		System.out.println( solution);
	}
	
	//@Test
	public void testCodecParent() {
		File logging = new File( target, "com.braintribe.codecs/1.0/CodecParent/pom.xml");
		Solution solution = test( logging);
		System.out.println( solution);
	}
	
	//@Test
	public void testCommonsParent() {
		File logging = new File( target, "com.braintribe.common/1.0/CommonParent/pom.xml");
		Solution solution = test( logging);		
		System.out.println( solution);
	}
	
	//@Test
	public void testPlatformApiTest() {
		File logging = new File( target, "com.braintribe.common/1.0/PlatformApiTest/pom.xml");
		Solution solution = test( logging);		
		System.out.println( solution);
	}


}
