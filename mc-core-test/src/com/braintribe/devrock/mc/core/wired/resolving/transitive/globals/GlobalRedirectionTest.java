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
