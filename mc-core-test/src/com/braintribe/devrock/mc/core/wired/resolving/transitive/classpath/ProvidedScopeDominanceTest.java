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

import org.junit.Test;

import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests a special feature for handling provided: 
 * 
 * the terminal's dependencies scoped as 'provided' will lead to the behavior that any other 
 * matching transitive dependency is also regarded to be of scope 'provided', regardless of
 * what it's actual scope is.   
 * 
 * 
 * @author pit
 *
 */

// 
public class ProvidedScopeDominanceTest extends AbstractClasspathResolvingTest {

	@Override
	protected RepoletContent archiveInput() {
		File file = new File( input, "provided.scope.dominance.definition.yaml");
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot parse file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 	
	}
	
	@Test
	public void testProvidedDominanceInRuntimeScope() {
		AnalysisArtifactResolution resolution = runAsArtifact("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().scope(ClasspathResolutionScope.runtime).done(), "scope-runtime");

		Validator validator = new Validator();
		
		// validate result - solutions 
		validator.validate( new File( input, "provided.scope.dominance.runtime.validation.yaml"), resolution);
		
		// validate result - terminal
		validator.validateTerminal( new File( input, "provided.scope.dominance.runtime.terminal.validation.yaml"), resolution);
	
		// validate that no error's returned 
		validator.validateFailedResolution(resolution, null, null);
				
		validator.assertResults();
	}
	
	@Test
	public void testProvidedDominanceInCompileScope() {
		AnalysisArtifactResolution resolution = runAsArtifact("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().scope(ClasspathResolutionScope.compile).done(), "scope-compile");

		Validator validator = new Validator();
		
		// validate result 
		validator.validate( new File( input, "provided.scope.dominance.compile.validation.yaml"), resolution);
		
		// validate result - terminal
		validator.validateTerminal( new File( input, "provided.scope.dominance.compile.terminal.validation.yaml"), resolution);
	
		// validate that no error's returned 
		validator.validateFailedResolution(resolution, null, null);
				
		validator.assertResults();

	}


	@Test
	public void testProvidedDominanceInTestScope() {
		AnalysisArtifactResolution resolution = runAsArtifact("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().scope(ClasspathResolutionScope.test).done(), "scope-test");

		Validator validator = new Validator();
		
		// validate result 
		validator.validate( new File( input, "provided.scope.dominance.test.validation.yaml"), resolution);
		
		// validate result - terminal
		validator.validateTerminal( new File( input, "provided.scope.dominance.test.terminal.validation.yaml"), resolution);
	
		// validate that no error's returned 
		validator.validateFailedResolution(resolution, null, null);
				
		validator.assertResults();

	}
}
