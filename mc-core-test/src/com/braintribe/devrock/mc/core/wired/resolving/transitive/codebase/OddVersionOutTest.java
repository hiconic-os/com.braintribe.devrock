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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.codebase;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests a group-codebase setup with one of the versions being the odd one out. Didn't expect any issues 
 * (old mc had issues, see COREDR-129), doesn't have any. 
 * 
 * @author pit
 *
 */
public class OddVersionOutTest extends AbstractCodebaseClasspathResolvingTest {

	protected RepoletContent archiveInput() {
		return RepoletContent.T.create();
	}

	@Test
	public void testPlainOnStructuralMetaGroup() {
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().enrichJar(false).done(), new File(input, "odd/codebase"), "${artifactId}");
		
		Assert.assertTrue("resolution unexpectedly failed", !resolution.hasFailed());
			
		Validator validator = new Validator(true);
		validator.validateYaml(new File(input, "odd/plain.validation.yaml"), resolution);
		validator.assertResults();		
	}
}
