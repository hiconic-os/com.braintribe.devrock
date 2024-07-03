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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.depmgt;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.braintribe.devrock.mc.core.compiler.configuration.origination.ReasoningHelper;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.mc.reason.MalformedManagedDependency;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests a simple setup with an improperly declared managed dependency
 * 
 * @author pit
 *
 */
public class InvalidDepMgtDependencyDeclarationResolutionTest extends AbstractDependencyManagementResolvingTest {

	@Override
	protected RepoletContent archiveInput() {	
		return loadInput( new File( input, "invalid.declared.depmgt.dependency.definition.yaml"));
	}
	

	@Test
	public void runSimpleParentStructure() {
		String terminal = "com.braintribe.devrock.test:t#1.0.1";
		AnalysisArtifactResolution artifactResolution = run(terminal, standardResolutionContext, false, null);
		
		Reason failure = artifactResolution.getFailure();
		
		Validator validator = new Validator();
		
		List<Reason> reasons = ReasoningHelper.extractAllReasons(failure, r -> r instanceof MalformedManagedDependency);
		
		validator.assertTrue("expected [1] MalformedManagedDependency reason in failure, yet found [" + reasons.size() + "]", reasons.size() == 1);
												
		validator.assertResults();
				
	}

	
}
