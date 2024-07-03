// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
