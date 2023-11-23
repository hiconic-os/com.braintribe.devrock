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
