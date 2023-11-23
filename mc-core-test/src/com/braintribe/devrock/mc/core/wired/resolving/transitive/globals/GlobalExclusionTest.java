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

import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests whether global exclusions are properly supported in the {@link TransitiveDependencyResolver} <br/>
 * 
 * @author pit
 *
 */
public class GlobalExclusionTest extends AbstractGlobalTransitiveResolverTest {

	@Override
	protected File archiveInput() {
		return new File( input, "global.exclusions.definition.txt");
	}
	
	@Test
	public void run() {		
		AnalysisArtifactResolution resolution = run( "com.braintribe.devrock.test:t#1.0.1", standardResolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		Validator validator = new Validator();
		validator.validateExpressive( new File ( input, "global.exclusions.validation.txt"), resolution);
		validator.assertResults();
	}

	
}
