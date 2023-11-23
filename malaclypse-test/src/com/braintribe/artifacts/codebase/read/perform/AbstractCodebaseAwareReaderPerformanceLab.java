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
package com.braintribe.artifacts.codebase.read.perform;

import java.io.File;
import java.util.UUID;

import org.junit.Assert;

import com.braintribe.artifacts.codebase.AbstractCodebaseAwareLab;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.CrcValidationLevel;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.panther.SourceRepository;



public abstract class AbstractCodebaseAwareReaderPerformanceLab extends AbstractCodebaseAwareLab{
	
	protected static void runbefore(String templateStr) {
		try {			
			runBefore( CrcValidationLevel.ignore);
			SourceRepository sourceRepository = SourceRepository.T.create();			
			sourceRepository.setRepoUrl( masterCodebase.toURI().toURL().toString());			
			groupingAwareResolverFactory.setSourceRepository(sourceRepository);	
			groupingAwareResolverFactory.setCodebaseTemplate( templateStr);
			pomExpertFactory.setDependencyResolverFactory(groupingAwareResolverFactory);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");
		}  
	}
	
	protected Solution singleRead(File file) {
		
		ArtifactPomReader reader = pomExpertFactory.getReader();
		
		try {
			long before = System.nanoTime();
			Solution solution = reader.readPom( UUID.randomUUID().toString(), file);
			long after = System.nanoTime();
			
			double rt = (new Double(after - before)) / 1E6;
			
			System.out.println( "reading [" + file.getAbsolutePath() + "] took [" + rt + "] ms");
			
			return solution;
		} catch (PomReaderException e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");
		}
		return null;
	}
	
	
	protected Solution multipleRead(File file, int run, int threshold) {
		
		ArtifactPomReader reader = pomExpertFactory.getReader();
		Solution solution = null;
		long sum = 0;
		
		for (int i = 0; i < run; i++) {
			
			try {
				long before = System.nanoTime();
				solution = reader.readPom( UUID.randomUUID().toString(), file);
				long after = System.nanoTime();
				
				if (i >= threshold) {
					long rt = after - before;
					sum += rt;
				}
				
			} catch (PomReaderException e) {
				e.printStackTrace();
				Assert.fail("exception [" + e + "] thrown");
			}
		}
		
		double avg = new Double(sum) / (run - threshold);
		double rt = avg / 1E6; 
			
		System.out.println( "reading [" + file.getAbsolutePath() + "] [" + (run-threshold) + "] times took [" + rt + "] ms on average");		
		return solution;
		
	}
	

	
	
}
