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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.pom;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class InvalidPomCompilingTest extends AbstractTransitiveResolverPomCompilingTest {

	protected RepoletContent archiveInput(String repoId) {
		File file = new File( input, repoId + ".content.definition.txt");
		try {
			return RepositoryGenerations.parseConfigurationFile(file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot load parser file [" + file.getAbsolutePath() + "]" , IllegalStateException::new);
		} 
	}
	
	@Override
	protected void runAdditionalBeforeSteps() {
		// copy initial data (mimic local repository)
		if (initial.exists()) {
			TestUtils.copy( initial, repo);
		}		
	}

	@Test
	// TODO: currently throws an exception - but it should flag the resolution as failed
	public void IncompleteArtifactCoordinatesTest() {
		String grpId = "com.braintribe.devrock.test";
		String artId = "x";
		String vrs = "1.0.2";
		AnalysisArtifactResolution res = runAnalysis(grpId + ":" + artId + "#" + vrs);	
		System.out.println(res.getFailure().stringify());
		Assert.assertTrue("analysis should be flagged as failed", res.hasFailed());
	}
	
	@Test
	// TODO : doesn't report error nor does it fail
	public void IncompleteParentTest() {
		String grpId = "com.braintribe.devrock.test";
		String artId = "x-parent";
		String vrs = "1.0.1";
		AnalysisArtifactResolution res = runAnalysis(grpId + ":" + artId + "#" + vrs);
		System.out.println(res.getFailure().stringify());
		Assert.assertTrue("analysis should be flagged as failed", res.hasFailed());
	}
	
	@Test
	// TODO: currently throws an exception - but it should flag the resolution as failed
	public void IncompleteDependencyTest() {
		String grpId = "com.braintribe.devrock.test";
		String artId = "x";
		String vrs = "1.0.1";
		AnalysisArtifactResolution res = runAnalysis(grpId + ":" + artId + "#" + vrs);
		System.out.println(res.getFailure().stringify());
		Assert.assertTrue("analysis should be flagged as failed", res.hasFailed());
	}
	
	@Test
	public void PropertyIncompleteDependencyTest() {
		
		/* TODO:
		 * Points to clarify with Pit
		 * - MalformedDependency is underspecified after resolution
		 * - UnresolvedDependency is overspecified after resolution
		 * - How to address the different need in different situations
		 * 
		 */
		
		String grpId = "com.braintribe.devrock.test";
		String artId = "p";
		String vrs = "1.0";
		AnalysisArtifactResolution res = runAnalysis(grpId + ":" + artId + "#" + vrs);
		
		if (res.hasFailed())
			System.out.println(res.getFailure().stringify());
		
		
		Assert.assertTrue("analysis should be flagged as failed", res.hasFailed());
	}
	
}
