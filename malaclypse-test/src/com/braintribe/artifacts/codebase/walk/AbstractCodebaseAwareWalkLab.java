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
package com.braintribe.artifacts.codebase.walk;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

import org.junit.Assert;

import com.braintribe.artifacts.codebase.AbstractCodebaseAwareLab;
import com.braintribe.artifacts.codebase.CodebaseGenerator;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.CrcValidationLevel;
import com.braintribe.build.artifact.walk.multi.WalkException;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifact.walk.multi.WalkerFactory;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.test.multi.ClashStyle;
import com.braintribe.test.multi.WalkDenotationTypeExpert;
import com.braintribe.test.multi.WalkingExpert;
import com.braintribe.test.multi.realRepoWalk.Monitor;
import com.braintribe.testing.category.SpecialEnvironment;

public abstract class AbstractCodebaseAwareWalkLab extends AbstractCodebaseAwareLab implements SpecialEnvironment{
	protected static File contents = new File("res/grouping");
	protected static File target;
	protected static File targetGrpOne;
	protected static File targetTerminal;
	private static File masterCodebase = new File( contents, "grouping.flattened");
	
	private static WalkerFactory walkerFactory;
	
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
			
			// walk
			
			Monitor monitor = new Monitor();
			monitor.setVerbosity( true);
									
			walkerFactory = walkerFactory(groupingAwareResolverFactory);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");
		}  
	}
	
	private Solution read(File file) {
		
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
	
	private void walk(File file, String [] expectedNames) {
		
		Solution terminal = read( file);
		WalkDenotationType walkType = WalkDenotationTypeExpert.buildCompileWalkDenotationType(ClashStyle.optimistic);
		Walker walker = walkerFactory.apply(walkType);
		
		try {
			Collection<Solution> walkResult = WalkingExpert.walk( UUID.randomUUID().toString(), terminal, expectedNames, walker, true);
			System.out.println(walkResult);
		} catch (WalkException e) {
			e.printStackTrace();	
			Assert.fail("exception [" + e + "] thrown");
		}
	}
	

	//@Test
	public void testGrpOne() {
		String [] expectedNames = new String[] { "com.braintribe.grpOne:C#1.0.1", "com.braintribe.grpBase:BaseDependency#1.0.1"};
		walk( targetGrpOne, expectedNames);
	}
	
	//@Test
	public void testTerminal() {
		String [] expectedNames = new String[] { 	// grpOne
													"com.braintribe.grpOne:A#1.0.1", 
													"com.braintribe.grpOne:C#1.0.1", 
													"com.braintribe.grpBase:BaseDependency#1.0.1",
													// grpOneSubOne
													"com.braintribe.grpOne.subOne:A#1.0.1",
													"com.braintribe.grpOne.subOne:B#1.0.1",
													// terminal
													"com.braintribe.terminal:A#1.0.1"};
		walk( targetTerminal, expectedNames);
	}
}
