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

import org.junit.Test;

import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests special scopes, i.e. 'magick' scopes and their effect on dependency scopes
 * @author pit
 *
 */

// TODO: test case for 'test' is returning 't-provided' (but not 'a-provided', 'b-provided'), no 'provided' should be there as it's a runtime cp for running tests?
public class SpecialScopeCombinationTest extends AbstractClasspathResolvingTest {

	@Override
	protected RepoletContent archiveInput() {
		File file = new File( input, "special.scopes.definition.yaml");
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot parse file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 	
	}
	
	@Test
	public void testRuntimeScope() {
		AnalysisArtifactResolution resolution = runAsArtifact("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().scope(ClasspathResolutionScope.runtime).done(), "scope-runtime");

		Validator validator = new Validator();
		
		// validate result 
		validator.validate( new File( input, "runtime.scope.validation.yaml"), resolution);
	
		// validate that no error's returned 
		validator.validateFailedResolution(resolution, null, null);
				
		validator.assertResults();
	}
	
	@Test
	public void testCompileScope() {
		AnalysisArtifactResolution resolution = runAsArtifact("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().scope(ClasspathResolutionScope.compile).done(), "scope-compile");

		Validator validator = new Validator();
		
		// validate result 
		validator.validate( new File( input, "compile.scope.validation.yaml"), resolution);
	
		// validate that no error's returned 
		validator.validateFailedResolution(resolution, null, null);
				
		validator.assertResults();

	}


	@Test
	public void testTestScope() {
		AnalysisArtifactResolution resolution = runAsArtifact("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().scope(ClasspathResolutionScope.test).done(), "scope-test");

		Validator validator = new Validator();
		
		// validate result 
		validator.validate( new File( input, "test.scope.validation.yaml"), resolution);
	
		// validate that no error's returned 
		validator.validateFailedResolution(resolution, null, null);
				
		validator.assertResults();

	}
}
