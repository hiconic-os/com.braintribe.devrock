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

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;


/**
 * tests the case where a simple dependency is unresolvable
 * 
 * @author pit
 *
 */
public class UnresolvedVariableTest extends AbstractUnresolvedHandlingTest{

	@Override
	protected RepoletContent archiveInput() {	
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( new File( input,"unresolved.variable.definition.yaml"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} 
		return null;
	}
	

	//@Test
	public void runUnresolvedVariableTestOnTDR() {
		List<String> expectedIncompleteArtifacts = new ArrayList<>();
		expectedIncompleteArtifacts.add( "com.braintribe.devrock.test:c#1.0.1");
		
		List<String> expectedUnresolvedDependencies = new ArrayList<>();
		expectedUnresolvedDependencies.add( "com.braintribe.devrock.test:missing#[1.0,1.1)/:jar");
				
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardTransitiveResolutionContext);
			if (dumpResults) {
				dump(new File( output, "unresolved-variable.dump.tdr.yaml"), resolution);
			}
			Validator validator = new Validator();
			validator.validateFailedResolution(resolution, expectedIncompleteArtifacts, expectedUnresolvedDependencies);			
			validator.assertResults();
			
		} catch (Exception e) {		
			Assert.fail("unexpectedly, the TDR did thrown an exception - if dependency is missing");
		}
	}
	
	@Test
	public void runUnresolvedVariableTestOnCPR() {
		boolean exceptionThrown = false;
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardClasspathResolutionContext);
			if (dumpResults) {
				dump(new File( output, "unresolved-variable.dump.cpr.yaml"), resolution);
			}
			if (!resolution.hasFailed()) {
				Assert.fail("unexpectedly, the resolution was sucessful - even if a dependency is missing");
			}			
		} catch (Exception e) {
			e.printStackTrace();
			exceptionThrown = true;
		}
		Assert.assertTrue("unexpectedly, the CPR didn't throw an exception if a dependency is missing", exceptionThrown);
	}
	
}
