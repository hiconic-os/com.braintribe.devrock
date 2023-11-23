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
 * tests that a resolution (restricted on pom files) works with single codebase repositories, in all 3 structual possiblities
 * 
 * NOTE: test currently runs only successfully if ran as standalone. If run within a group (and 'release' run) it will fail. 
 * 
 * @author pit / dirk
 *
 */
public class PlainCodebaseClasspathResolvingTest extends AbstractCodebaseClasspathResolvingTest {
	
	private List<Pair<File,Boolean>> filesToCheckOnExistance = new ArrayList<>();
	{
		filesToCheckOnExistance.add( Pair.of( new File( repo, "last-probing-result-archive.yaml"), true));
	}
	
	@Override
	protected RepoletContent archiveInput() {
		return RepoletContent.T.create();		
	}
	
		
	/**
	 * tests a 'standard BT style codebase structure' for a single group, i.e. building within a single group directory 
	 */
	@Test
	public void testPlainOnStructuralGroup() {
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().enrichJar(false).done(), new File(input, "structural.group/complete"), "${artifactId}");		

		Validator validator = new Validator(true);
		validator.validate(new File(input, "structural.group/plain.validation.txt"), resolution);
		validator.validateFileExistance( filesToCheckOnExistance);		 		
		validator.assertResults();
	}
	
	/**
	 * tests a 'standard BT style codebase structure' for multiple groups, i.e. building across multiple group directories
	 */
	@Test
	public void testPlainOnStructuralMetaGroup() {
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test.t:t#1.0.1", ClasspathResolutionContext.build().enrichJar(false).done(), new File(input, "structural.metagroup/complete"), "${groupId}/${artifactId}");
	
		Validator validator = new Validator(true);
		validator.validate(new File(input, "structural.metagroup/plain.validation.txt"), resolution);
		validator.validateFileExistance( filesToCheckOnExistance);
		validator.assertResults();
	}
	
	/**
	 *  tests a codebase that is structured like a standard 'maven repository', i.e. building in a 'old BT style' structure
	 */
	@Test
	public void testPlainOnStructuralMaven() {		
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test.t:t#1.0.1", ClasspathResolutionContext.build().enrichJar(false).done(), new File(input, "structural.maven/complete"), "${groupId.expanded}/${artifactId}/${version}");
		
		Validator validator = new Validator(true);
		validator.validate(new File(input, "structural.maven/plain.validation.txt"), resolution);
		validator.validateFileExistance( filesToCheckOnExistance);
		validator.assertResults();		
	}

	
}
