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

// TODO: update test to also check ADDITONALLY incomplete artifacts after leniency levels in POM compiler have been implemented 

/**
 * tests the reporting of failed resolutions. 
 * 
 * Currently, only unresolved dependencies are reported, as the leniency for the POM compiler isn't routed yet. 
 * @author pit
 *
 */
public class FailingClasspathResolutionTest extends AbstractClasspathResolvingTest {
	private List<String> expectedUnresolvedDependencies = new ArrayList<>();
	{
		expectedUnresolvedDependencies.add( "com.braintribe.devrock.nirvana:oops#1.0.1/:jar");
	}
	private List<String> expectedIncompleteArtifacts = new ArrayList<>();
	{
		expectedIncompleteArtifacts.add( "com.braintribe.devrock.test:b#1.0.1");
	}
	
	@Override
	protected RepoletContent archiveInput() {	
		File file = new File( input, "simpleClashingTree.unresolved.definition.txt");
		try {
			return RepositoryGenerations.parseConfigurationFile( file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot parse file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 
	}	
	
	
	
	@Test
	public void testFailureReportingOnUnresolvedDependencies() {
		ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build() //
			.clashResolvingStrategy(ClashResolvingStrategy.firstOccurrence) // 
			.lenient(true) // 
			.scope(ClasspathResolutionScope.compile) //
			.done(); //
		
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#[1.0,1.1)", resolutionContext, "failing-cr");
		
		Validator validator = new Validator();
		// validate result 
		validator.validateExpressive( new File( input, "simpleClashingTree.unresolved.validation.txt"), resolution);
		
		// validate clashes 
		validator.validateFailedResolution(resolution, expectedIncompleteArtifacts, expectedUnresolvedDependencies);
		
		validator.assertResults();
		
	}
}
