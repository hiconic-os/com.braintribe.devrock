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
 * tests that within injected codebase repositories, the order of their appearance in the configuration is respected
 * 
 * @author pit
 *
 */
public class CodebaseSequentialResolvingTest extends AbstractCodebaseClasspathResolvingTest {
	
	private List<Pair<File,Boolean>> filesToCheckOnExistance = new ArrayList<>();
	{
		filesToCheckOnExistance.add( Pair.of( new File( repo, "last-probing-result-archive.yaml"), true));
	}

	@Override
	protected RepoletContent archiveInput() {	
		return RepoletContent.T.create();
	}

	
	@Test
	public void testDominanceInSequence() {
	
		Pair<File,String> p1 = Pair.of( new File( input, "multiple/codebase-1"), "${artifactId}");
		Pair<File,String> p2 = Pair.of( new File( input, "multiple/codebase-2"), "${artifactId}");
		
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().enrichJar(false).done(), p1, p2);
		
		Validator validator = new Validator(true);
		validator.validate(new File(input, "multiple/multiple.plain.validation.txt"), resolution);
		validator.validateFileExistance( filesToCheckOnExistance);
		//validator.validateFileExistance( metadataToCheckOnExistance);
		validator.assertResults();		
	}

}
