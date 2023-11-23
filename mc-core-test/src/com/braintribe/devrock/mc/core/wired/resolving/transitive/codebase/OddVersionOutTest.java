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
