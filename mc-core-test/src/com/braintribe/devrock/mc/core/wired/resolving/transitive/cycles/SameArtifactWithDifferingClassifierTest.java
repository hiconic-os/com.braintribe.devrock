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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.cycles;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests the case of the same 'leaf artifact' occurring twice with differing classifiers
 * 
 * t -> a,b & a -> c:one, b -> c:two
 *  
 * @author pit
 *
 */
public class SameArtifactWithDifferingClassifierTest extends AbstractTransitiveCycleTest {

	@Override
	protected File archiveInput() {
		return new File( input, "dependency.differing.classifier.reference.definition.yaml");
	}
	
	protected File validationInput() {
		return new File( input, "dependency.differing.classifier.reference.validation.yaml");
	}


	@Test
	public void runTDR() {
		Pair<AnalysisArtifactResolution,Long> pair = resolveAsArtifact(terminal, standardTransitiveResolutionContext);
		AnalysisArtifactResolution resolution = pair.first;
		
		if (resolution.hasFailed()) {
			Assert.fail( resolution.getFailure().asFormattedText());			
		}
		else {
			Validator validator = new Validator();
			validator.validate( validationInput(), resolution, false, false);
			validator.assertResults();							
		}
	}
	
	@Test
	public void runCPR() {
		Pair<AnalysisArtifactResolution,Long> pair = resolveAsArtifact(terminal, standardClasspathResolutionContext);
		AnalysisArtifactResolution resolution = pair.first;
		
		if (resolution.hasFailed()) {
			Assert.fail( resolution.getFailure().asFormattedText());
		}
		else {		
			Validator validator = new Validator();
			validator.validate( validationInput(), resolution, true, false);
			validator.assertResults();					
		}
	}
	
}
