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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.globals;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.testing.category.KnownIssue;

/**
 * tests whether global redirections are properly supported in the {@link TransitiveDependencyResolver}
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class GlobalRedirectionTest extends AbstractGlobalTransitiveResolverTest {

	@Override
	protected File archiveInput() {
		return new File( input, "global.redirects.definition.txt");
	}
	
	@Test
	public void runGlobalRedirectionTest() {		
		AnalysisArtifactResolution resolution = run( "com.braintribe.devrock.test:t#1.0.2", standardResolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		/*
		Validator.validateExpressive( new File ( input, "global.redirects.validation.txt"), resolution);
		*/
	}

}
