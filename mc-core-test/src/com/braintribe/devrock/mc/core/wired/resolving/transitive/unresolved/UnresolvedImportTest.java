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

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;


/**
 * tests an unresolvable import dependency in the dependency management of the parent
 * 
 * 
 * 
 * @author pit
 *
 */
public class UnresolvedImportTest extends AbstractUnresolvedHandlingTest{

	@Override
	protected RepoletContent archiveInput() {	
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( new File( input,"unresolved.import.definition.yaml"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} 
		return null;
	}

	@Test
	public void runUnresolvedImportTestOnTDR() {
		boolean exceptionThrown = false;
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardTransitiveResolutionContext);
			if (!resolution.hasFailed()) {
				Assert.fail("unexpectedly, the resolution was sucessful - even if an import is missing");
			}			
			if (dumpResults) {
				dump(new File( output, "unresolved-import.dump.tdr.yaml"), resolution);
			}
			// validate reasoning here
			
		} catch (Exception e) {
			exceptionThrown = true;
		}
		Assert.assertTrue("expectedly, the TDR did throw an exception - if an import is missing", !exceptionThrown);
	}
	
	@Test
	public void runUnresolvedImportTestOnCPR() {
		boolean exceptionThrown = false;
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardClasspathResolutionContext);
			if (!resolution.hasFailed()) {
				Assert.fail("unexpectedly, the resolution was sucessful - even if an import is missing");
			}			
		} catch (Exception e) {
			exceptionThrown = true;
		}
		Assert.assertTrue("unexpectedly, the CPR didn't throw an exception if an import is missing", exceptionThrown);
	}
	
}
