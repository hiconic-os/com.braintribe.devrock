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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.pom;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests a situation where scopes from a dependency-management declaration overrides the scope of the actual referencing dependency. 
 * @author pit
 *
 */
public class ScopeTransferFromDepMgtTest extends AbstractTransitiveResolverPomCompilingTest {

	@Override
	protected RepoletContent archiveInput(String key) {		
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( new File( input, "scope.transfer.definition.yaml"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");			
		}
		return null;
	}
	
	
	@Override
	protected void runAdditionalBeforeSteps() {
		// no op		
	}


	@Test
	public void runScopeTransferTest() {
		AnalysisArtifactResolution resolution = runAnalysis("com.braintribe.devrock.test:t#1.0.1");
		
		Validator validator = new Validator();
		
		validator.validate( new File( input, "scope.transfer.validation.yaml"), resolution);		
		validator.assertResults();
	}

}
