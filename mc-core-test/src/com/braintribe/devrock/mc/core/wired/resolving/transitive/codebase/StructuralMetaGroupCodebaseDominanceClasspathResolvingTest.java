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
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests that the codebase repository is 'dominant' to a remote repository, i.e. its results (if any) override the results of the remote repository.
 * tests a 'structural meta group' codebase situation, i.e. across multiple groups (as in our local branch codebases) 
 * @author pit / dirk
 *
 */
public class StructuralMetaGroupCodebaseDominanceClasspathResolvingTest extends AbstractCodebaseClasspathResolvingTest {
	
	private List<Pair<File,Boolean>> filesToCheckOnExistance = new ArrayList<>();
	{
		filesToCheckOnExistance.add( Pair.of( new File( repo, "last-probing-result-archive.yaml"), true));
	}

	@Override
	protected RepoletContent archiveInput() {
		return archiveInput("structural.metagroup/dominance.definition.txt");
	}
	
	/**
	 * tests that the remote repository 'archive' is ignored if the codebase repository has results for an artifact
	 */
	@Test
	public void testDominance() {
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test.t:t#1.0.1", ClasspathResolutionContext.build().enrichJar(false).done(), new File(input, "structural.metagroup/complete"), "${groupId}/${artifactId}");
		Validator validator = new Validator( true);
		validator.validate(new File(input, "structural.metagroup/plain.validation.txt"), resolution);
		validator.validateFileExistance( filesToCheckOnExistance);
		validator.assertResults();
	}

	/**
	 * tests that the remote repository 'archive' is used if the codebase repository has no results for an artifact
	 */
	@Test
	public void testMixed() {
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test.t:t#1.0.1", ClasspathResolutionContext.build().enrichJar(false).done(), new File(input, "structural.metagroup/incomplete"), "${groupId}/${artifactId}");
		Validator validator = new Validator( true);
		validator.validate(new File(input, "structural.metagroup/mixed.validation.txt"), resolution);
		validator.validateFileExistance( filesToCheckOnExistance);
		validator.assertResults();
	}
	
}
