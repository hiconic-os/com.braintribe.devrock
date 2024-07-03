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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.classpath.noncontributing;

import java.io.File;

import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests how a dependency with a 'non-contributing content' (from the point of view of the classpath) is treated. 
 * Currently, the solution appears in the CPR's result - and may create issues in post processors that do not expect 
 * non-contributing classifiers like 'javadoc:jar' (see AD-2582).
 * 
 * However, differing from old mc, mc-ng *is* removing dependencies that are filtered-out. 
 * 
 * @author pit
 *
 */
public class JavadocReferenceInClasspathTest extends AbstractNonContributingClasspathTest {

	@Override
	protected File archiveInput() {
		return new File( input, "nonContributing.classifier.selfreference.definition.yaml");
	}
	private File validationInputForTdr() {
		return new File( input, "tdr.nonContributing.classifier.selfreference.validation.yaml");
	}
	private File validationInputForCpr() {
		return new File( input, "cpr.nonContributing.classifier.selfreference.validation.yaml");
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
			validator.validate( validationInputForTdr(), resolution, false, false);
			validator.assertResults();		
		}
	}
	
	/**
	 * works currently because a-javadoc:jar is enriched as it's a 'well known part' and gets enriched because of the 
	 * first a reference.
	 */
	@Test
	public void runCPR() {
		Pair<AnalysisArtifactResolution,Long> pair = resolveAsArtifact(terminal, standardClasspathResolutionContext);
		AnalysisArtifactResolution resolution = pair.first;
		
		if (resolution.hasFailed()) {
			System.out.println( resolution.getFailure().asFormattedText());			
		}
		else {
			Validator validator = new Validator();
			validator.validate( validationInputForCpr(), resolution, true, false);
			validator.assertResults();			
		}
	}
	

}
