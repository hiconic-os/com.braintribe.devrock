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

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.version.Version;

public class SimpleTreeTest extends AbstractTransitiveResolverTest {

	@Override
	protected File archiveInput() {
		return new File( input, "simpleTree.definition.txt");
	}
	
	
	@Test
	public void testWithDependencyTerminal() {
		
		AnalysisArtifactResolution resolution = run( "com.braintribe.devrock.test:a#1.0.1", standardResolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		Validator validator = new Validator();
		validator.validate( new File ( input, "simpleTree.validation.yaml"), resolution);
		validator.assertResults();
	}
	
	@Test
	public void testWithArtifactTerminal() {
		CompiledArtifact artifact = CompiledArtifact.T.create();
		artifact.setGroupId("com.braintribe.devrock.test");
		artifact.setArtifactId("virtual-artifact");
		artifact.setVersion(Version.parse("1.0"));
		artifact.getDependencies().add(CompiledDependency.from(CompiledDependencyIdentification.parse("com.braintribe.devrock.test:a#1.0.1")));
		
		AnalysisArtifactResolution resolution = run( CompiledTerminal.from(artifact), standardResolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		Validator validator = new Validator();
		validator.validate( new File ( input, "simpleTree.validation.yaml"), resolution);
		validator.assertResults();
	}

}
