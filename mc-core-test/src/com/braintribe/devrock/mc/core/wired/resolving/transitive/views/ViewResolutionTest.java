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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.views;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class ViewResolutionTest extends AbstractViewResolvingTest {

	@Override
	protected RepoletContent archiveInput() {
		return archiveInput( "archive.definition.yaml");
	}
	
	@Test
	public void runViewResolutionTest() {
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", TransitiveResolutionContext.build().done());
			Validator validator = new Validator();
			validator.validate(new File( input, "archive.validation.yaml"), resolution);
			validator.assertResults();			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("unexpected exception [" + e.getMessage() + "]");
			
		}
		System.out.println();
	}
	


}
