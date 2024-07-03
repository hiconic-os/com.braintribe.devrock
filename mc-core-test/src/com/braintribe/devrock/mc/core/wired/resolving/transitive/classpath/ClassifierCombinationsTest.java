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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.classpath;

import java.io.File;

import org.junit.Test;

import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests a case where the same artifact has two references with differing classifiers.
 * 
 * @author pit
 *
 */
public class ClassifierCombinationsTest extends AbstractClasspathResolvingTest {
	@Override
	protected RepoletContent archiveInput() {		
		return loadInput( new File( input, "classifier.combinations.definition.yaml"));
	}

	@Test 
	public void runClassifierCombinationsTest() {
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().scope(ClasspathResolutionScope.compile).done(), "classifier-combinations");
		
		Validator validator = new Validator();
		validator.validate( new File( input, "classifier.combinations.definition.yaml"), resolution, true, false);
		
		validator.assertResults();
		
	}

}
