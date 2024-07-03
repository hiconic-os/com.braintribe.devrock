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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.unresolved;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.testing.category.KnownIssue;

/**
 * tests the behavior of TDR & CPR about unresolved redirection targets, i.e. if an artifact actually redirects to another 
 * artifact, and this one is missing.
 * 
 * TDR: correctly flags the resolution as invalid, has the correct reasons   
 * CPR: correctly throws an exception, reasoning is taken from TDR, ergo fine. 
 * @author pit
 *
 *
 * NOTE : the unresolved dependency is TWICE in the resolution which is due to the fact that there are two different 
 * dependencies pointing to the same unresolved relocation. 
 */


public class UnresolvedRedirectionTest extends AbstractUnresolvedHandlingTest{

	@Override
	protected RepoletContent archiveInput() {	
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( new File( input,"unresolved.redirection.definition.yaml"));
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
