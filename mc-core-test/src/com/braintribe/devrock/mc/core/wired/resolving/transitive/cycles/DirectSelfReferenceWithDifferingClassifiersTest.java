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

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.testing.category.KnownIssue;

/**
 * t -> a-:jar -> a-one:jar
 * 
 * NOTE : CPR run fails (a is not reconsidered and therefore a-one:jar is not enriched  
 * 
 * @author pit
 *
 */
public class DirectSelfReferenceWithDifferingClassifiersTest extends AbstractTransitiveCycleTest {

	@Override
	protected File archiveInput() {
		return new File( input, "classifier.selfreference.definition.yaml");
	}
	
	private File validationInput() {
		return new File( input, "classifier.selfreference.validation.yaml");
	}


	@Test
	public void runTDR() {
		Pair<AnalysisArtifactResolution,Long> pair = resolveAsArtifact(terminal, standardTransitiveResolutionContext);
		AnalysisArtifactResolution resolution = pair.first;
		
		if (resolution.hasFailed()) {
			System.out.println( resolution.getFailure().asFormattedText());
		}
		else {
			Validator validator = new Validator();
			validator.validate( validationInput(), resolution, false, false);
			validator.assertResults();		
		}
	}
	
	/**
	 * fails currently because a:one is not enriched (and invalidated by the CPR itself via Exception)
	 */
	@Test
	@Category(KnownIssue.class)
	public void runCPR() {
		Pair<AnalysisArtifactResolution,Long> pair = resolveAsArtifact(terminal, standardClasspathResolutionContext);
		AnalysisArtifactResolution resolution = pair.first;
		
		if (resolution.hasFailed()) {
			System.out.println( resolution.getFailure().asFormattedText());			
		}
		else {
			Validator validator = new Validator();
			validator.validate( validationInput(), resolution, true, false);
			validator.assertResults();			
		}
	}
	
	
}
