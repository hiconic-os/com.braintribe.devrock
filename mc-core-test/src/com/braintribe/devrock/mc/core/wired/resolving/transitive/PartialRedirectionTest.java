// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
