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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.invalid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests the behavior of TDR & CPR about unresolved redirection targets, i.e. if an artifact actually redirects to another 
 * artifact, and this one is missing.
 * 
 * TDR: correctly flags the resolution as invalid, points to the right artifact, yet the reason why the redirecting artifact 
 * was invalid is not reported.
 *    
 * @author pit
 * 
 */


public class InvalidRedirectionTest extends AbstractInvalidHandlingTest{

	@Override
	protected RepoletContent archiveInput() {	
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( new File( input,"invalid.redirection.definition.yaml"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} 
		return null;
	}

	@Test
	public void runUnresolvedRedirectionTargetTestOnTDR() {
		List<String> expectedIncompleteArtifacts = new ArrayList<>();
		expectedIncompleteArtifacts.add( "com.braintribe.devrock.test:a#1.0.1");
		expectedIncompleteArtifacts.add( "com.braintribe.devrock.test:b#1.0.1");
		
		List<String> expectedUnresolvedDependencies = new ArrayList<>();
		expectedUnresolvedDependencies.add( "com.braintribe.devrock.test:c#1.0.1/:jar");
		expectedUnresolvedDependencies.add( "com.braintribe.devrock.test:c#1.0.1/:jar");
		
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardTransitiveResolutionContext);
			
			Validator validator = new Validator();
			validator.validateFailedResolution(resolution, expectedIncompleteArtifacts, expectedUnresolvedDependencies);			
			validator.assertResults();
			
			// validate reasoning
			
			if (dumpResults) {
				dump(new File( output, "unresolved-redirection.dump.tdr.yaml"), resolution);
			}
			
		} catch (Exception e) {
			Assert.fail("unexpectedly, the TDR did throw an exception if a redirection target is missing");
		}
		
	}
	
	@Test
	public void runUnresolvedRedirectionTargetTestOnCPR() {
		boolean exceptionThrown = false;
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardClasspathResolutionContext);
			if (!resolution.hasFailed()) {
				Assert.fail("unexpectedly, the resolution was sucessful - even if a redirection target is missing");
			}
		} catch (Exception e) {
			exceptionThrown = true;
		}
		Assert.assertTrue("unexpectedly, the CPR didn't throw an exception if a redirection target is missing", exceptionThrown);
	}
	
}
