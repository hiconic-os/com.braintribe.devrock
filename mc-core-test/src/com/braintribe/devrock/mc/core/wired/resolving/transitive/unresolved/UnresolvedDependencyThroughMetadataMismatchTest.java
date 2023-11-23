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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.unresolved;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;


/**
 * tests the case where a ranged dependency is unresolvable because even if it's listed in the first-level metadata, it doesn't exist
 * @author pit
 *
 */
public class UnresolvedDependencyThroughMetadataMismatchTest extends AbstractUnresolvedHandlingTest{

	@Override
	protected RepoletContent archiveInput() {	
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( new File( input,"unresolved.dependency.definition.yaml"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} 
		return null;
	}
	

	
	@Override
	protected void additionalSetupTask() {
		// copy the test data 
		File initial = new File( input, "initial");
		TestUtils.copy(initial, repo);
	}



	@Test
	public void runUnresolvedDependencyTestOnTDR() {
		List<String> expectedIncompleteArtifacts = new ArrayList<>();
		expectedIncompleteArtifacts.add( "com.braintribe.devrock.test:t#1.0.1");
		
		List<String> expectedUnresolvedDependencies = new ArrayList<>();
		expectedUnresolvedDependencies.add( "com.braintribe.devrock.test:a#[1.0,1.1)/:jar");
				
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardTransitiveResolutionContext);
			Validator validator = new Validator();
			validator.validateFailedResolution(resolution, expectedIncompleteArtifacts, expectedUnresolvedDependencies);			
			validator.assertResults();
			
			if (dumpResults) {
				dump(new File( output, "unresolved-dependency-through-metadata-mismatch.dump.tdr.yaml"), resolution);
			}
		} catch (Exception e) {		
			Assert.fail("unexpectedly, the TDR did thrown an exception - if dependency is missing");
		}
	}
	
	@Test
	public void runUnresolvedDependencyTestOnCPR() {
		boolean exceptionThrown = false;
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardClasspathResolutionContext);
			if (!resolution.hasFailed()) {
				Assert.fail("unexpectedly, the resolution was sucessful - even if a dependency is missing");
			}			
		} catch (Exception e) {
			exceptionThrown = true;
		}
		Assert.assertTrue("unexpectedly, the CPR didn't throw an exception if a dependency is missing", exceptionThrown);
	}
	
}
