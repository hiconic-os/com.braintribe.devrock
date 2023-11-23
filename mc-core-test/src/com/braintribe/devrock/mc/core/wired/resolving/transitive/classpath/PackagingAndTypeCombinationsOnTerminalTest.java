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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.classpath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.ClashResolvingStrategy;

/**
 * tests invalid combinations of 'artifact packaging' and 'dependency type' on dependencies of the terminal 
 * (linked to the test that does it on an artifact within the tree)
 * 
 *  
 * @author pit
 *
 */

//TODO : currently, the incomplete artifacts don't have the reason for their incompleteness attached - they appear only in failure of the resolution.
// the test doesn't test the existence of the appropriate reasons, only the offending artifact is listed. 
 

public class PackagingAndTypeCombinationsOnTerminalTest extends AbstractClasspathResolvingTest {	
	private List<String> expectedIncompleteArtifacts = new ArrayList<>();
	{
		expectedIncompleteArtifacts.add( "com.braintribe.devrock.test:t#1.0.1");
	}

	@Override
	protected RepoletContent archiveInput() {	
		File file = new File( input, "invalidPackagingOnTerminal.definition.txt");
		try {
			return RepositoryGenerations.parseConfigurationFile( file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot parse file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 
	}	
	
	
	
	@Test
	public void runPackagingAndTypeCombinationsOnTerminalTest() {
		ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build() //
			.clashResolvingStrategy(ClashResolvingStrategy.firstOccurrence) // 
			.lenient(true) // 
			.scope(ClasspathResolutionScope.compile) //
			.done(); //
		
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#[1.0,1.1)", resolutionContext, "packaging-type-combination-terminal");
		
		Validator validator = new Validator();
		// validate result 
		validator.validateExpressive( new File( input, "invalidPackagingOnTerminal.validation.txt"), resolution);
		
		// validate clashes 
		validator.validateFailedResolution(resolution, expectedIncompleteArtifacts, null);
		
		validator.assertResults();
	}
}
