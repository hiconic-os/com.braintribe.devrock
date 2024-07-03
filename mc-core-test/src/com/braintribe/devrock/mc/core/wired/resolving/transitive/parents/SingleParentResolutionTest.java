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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.parents;

import java.io.File;

import org.junit.Test;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests a simple setup with one parent and one import inside this parent
 * 
 * @author pit
 *
 */
public class SingleParentResolutionTest extends AbstractParentResolutionResolvingTest {

	@Override
	protected RepoletContent archiveInput() {	
		return loadInput( new File( input, "parents.definition.yaml"));
	}
	

	@Test
	public void runSimpleParentStructure() {
		String terminal = "com.braintribe.devrock.test:t#1.0.1";
		AnalysisArtifactResolution artifactResolution = run(terminal, standardResolutionContext, false, null);
		
		Validator validator = new Validator();		
		validator.validate( new File( input, "parents.validation.yaml"), artifactResolution);
		boolean valid = validator.assertResults();
		
		if (!valid) {
			return;
		}
		
		// dump
		String name = buildDumpFilename(terminal, "enriched");
		dump(new File( output, name), artifactResolution);
		
		
	}

	
}
