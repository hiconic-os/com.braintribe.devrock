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
package com.braintribe.devrock.mc.core.wired.resolving.transitive;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class PartialRedirectionTest extends AbstractTransitiveResolverTest {

	@Override
	protected File archiveInput() {
		return new File( input, "partial.redirectionTree.definition.yaml");
	}
		
	@Test
	public void runWithPartialRedirections() {
		TransitiveResolutionContext resolutionContext = TransitiveResolutionContext.build()
				.includeRelocationDependencies( true)
				.done();
		AnalysisArtifactResolution resolution = run( "com.braintribe.devrock.test:a#1.0.1", resolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		Validator validator = new Validator();
		validator.validateYaml( new File ( input, "partial.redirectionTree.validation.yaml"), resolution);
		validator.assertResults();
	}
	
	@Test
	public void runWithPartialRedirectionsExcluding() {
		TransitiveResolutionContext resolutionContext = TransitiveResolutionContext.build()
				.includeRelocationDependencies( false)
				.done();
		AnalysisArtifactResolution resolution = run( "com.braintribe.devrock.test:a#1.0.1", resolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		Validator validator = new Validator();
		validator.validateYaml( new File ( input, "partial.redirectionTree.excluding.validation.yaml"), resolution);
		validator.assertResults();
	}
	

}
