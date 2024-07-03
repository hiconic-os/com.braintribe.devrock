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
 * tests how the two resolvers react on a missing parent
 * TDR : correctly flags the resolution, yet the reason why the parent was invalid doesn't show 
 * CPR : 
 * 
 * @author pit
 *
 */
public class InvalidParentTest extends AbstractInvalidHandlingTest{

	@Override
	protected RepoletContent archiveInput() {	
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( new File( input,"invalid.parent.definition.yaml"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} 
		return null;
	}

	@Test
	public void runUnresolvedParentTestOnTDR() {
		List<String> expectedIncompleteArtifacts = new ArrayList<>();
		expectedIncompleteArtifacts.add( "com.braintribe.devrock.test:a#1.0.1");
		expectedIncompleteArtifacts.add( "com.braintribe.devrock.test:t#1.0.1");
		
		
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardTransitiveResolutionContext);
			
			Validator validator = new Validator();
			validator.validateFailedResolution(resolution, expectedIncompleteArtifacts, null);			
			validator.assertResults();
			if (dumpResults) {
				dump(new File( output, "unresolved-parent.dump.tdr.yaml"), resolution);
			}
			
		} catch (Exception e) {
			Assert.fail("unexpectedly, the TDR did thrown an exception - even if parent is missing");
		}
	}
	
	@Test
	public void runUnresolvedParentTestOnCPR() {
		boolean exceptionThrown = false;
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardClasspathResolutionContext);
			if (!resolution.hasFailed()) {
				Assert.fail("unexpectedly, the resolution was sucessful - even if a parent is missing");
			}			
		}
		catch (Exception e) {
			exceptionThrown = true;
		}
		
		Assert.assertTrue("unexpectedly, the CPR didn't throw an exception if a parent is missing", exceptionThrown);
	}
	
}
