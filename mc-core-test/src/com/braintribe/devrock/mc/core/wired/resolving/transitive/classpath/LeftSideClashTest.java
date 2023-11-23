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
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.ClashResolvingStrategy;
import com.braintribe.model.resource.FileResource;

/**
 * tests case https://docs.google.com/drawings/d/1DMIErztsLVXLrnruwdAWqdk35dkeoEiSOPbGsRjoAQA, left side
 * 
 *  tests a dependency tree with clashes where both {@link ClashResolvingStrategy} deliver the same result
 *  
 * @author pit
 *
 */
public class LeftSideClashTest extends AbstractClasspathResolvingTest {
	private static final boolean verbose = false;
	
	@Override
	protected RepoletContent archiveInput() {	
		File file = new File( input, "simpleClashingTree.txt");
		try {
			return RepositoryGenerations.parseConfigurationFile( file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot parse file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 
	}	
	
	@Test
	public void testLeftSideFirstOccurrence() {
		ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build() //
			.clashResolvingStrategy(ClashResolvingStrategy.firstOccurrence) // 
			.lenient(false) // 
			.scope(ClasspathResolutionScope.compile) //
			.done(); //
		
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#[1.0,1.1)", resolutionContext, "left-side");
		
		if (verbose) {
			for (AnalysisArtifact artifact: resolution.getSolutions()) {
				System.out.println(artifact.asString());
				
				getCpJarParts(artifact).forEach(p -> System.out.println(" - " + ((FileResource)p.getResource()).getPath())); 
			}
		}
		
		Validator validator = new Validator();
		validator.validateExpressive( new File( input, "simpleClashingTree.validation.txt"), resolution);
		validator.assertResults();
	}
		

	@Test
	public void testLeftSideHighestVersion() {
		ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build() //
				.clashResolvingStrategy(ClashResolvingStrategy.highestVersion) // 
				.lenient(false) // 
				.scope(ClasspathResolutionScope.compile) //
				.done(); //
		
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#[1.0,1.1)", resolutionContext, "left-side-highest");
		
		if (verbose) {
			for (AnalysisArtifact artifact: resolution.getSolutions()) {
				System.out.println(artifact.asString());
				
				getCpJarParts(artifact).forEach(p -> System.out.println(" - " + ((FileResource)p.getResource()).getPath())); 
			}
		}
		Validator validator = new Validator();
		validator.validateExpressive( new File( input, "simpleClashingTree.validation.txt"), resolution);
		validator.assertResults();
	}
}
