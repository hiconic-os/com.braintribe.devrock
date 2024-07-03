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

import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class SimpleImportingReferenceTreeTest extends AbstractTransitiveResolverTest {

	@Override
	protected File archiveInput() {
		return new File( input, "simpleImportingReferenceTree.definition.flow.yaml");
	}
	
	
	@Test
	public void runWithoutImportsAndParents() {
		AnalysisArtifactResolution resolution = run( "com.braintribe.devrock.test:a#1.0.1", standardResolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		Validator validator = new Validator();
		validator.validate( new File ( input, "simpleImportingReferenceTree.validation.flow.yaml"), resolution);
		validator.assertResults();
	}

	@Test
	public void runWithImportsAndParents() {
		TransitiveResolutionContext resolutionContext = TransitiveResolutionContext.build()
				.includeImportDependencies(true)
				.includeParentDependencies(true)
				.done();
		AnalysisArtifactResolution resolution = run( "com.braintribe.devrock.test:a#1.0.1", resolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		Validator validator = new Validator();
		validator.validate( new File ( input, "simpleImportingReferenceTree.parent.validation.flow.yaml"), resolution);
		validator.assertResults();
	}
}
