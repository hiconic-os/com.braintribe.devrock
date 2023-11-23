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
package com.braintribe.artifacts.codebase.walk.perform;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

import org.junit.Assert;

import com.braintribe.artifacts.codebase.AbstractCodebaseAwareLab;
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

public abstract class AbstractCodebaseAwareWalkPerformanceLab extends AbstractCodebaseAwareLab implements SpecialEnvironment{
	protected static File masterCodebase;
	
	private static WalkerFactory walkerFactory;
	
	
	
	protected static void runbefore(String templateStr) {
		try {
			runBefore( CrcValidationLevel.ignore);
		
			SourceRepository sourceRepository = SourceRepository.T.create();
			sourceRepository.setRepoUrl( masterCodebase.toURI().toURL().toString());			
		
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
	protected void walk(File file, String [] expectedNames) {
		walk( file, expectedNames, 1, 0);
	}
	
	protected Collection<Solution> walk(File file, String [] expectedNames, int run, int threshold) {		
		Solution terminal = read( file);
		WalkDenotationType walkType = WalkDenotationTypeExpert.buildCompileWalkDenotationType(ClashStyle.optimistic);
		Walker walker = walkerFactory.apply(walkType);
		
		try {
			Collection<Solution> walkResult = WalkingExpert.walk( UUID.randomUUID().toString(), terminal, expectedNames, run, threshold, walker, true);
			return walkResult;			
		} catch (WalkException e) {
			e.printStackTrace();	
			Assert.fail("exception [" + e + "] thrown");
			return null;
		}
	}
	
}
