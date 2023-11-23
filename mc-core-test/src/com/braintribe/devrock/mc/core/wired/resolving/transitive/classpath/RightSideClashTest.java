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
 * tests case https://docs.google.com/drawings/d/1DMIErztsLVXLrnruwdAWqdk35dkeoEiSOPbGsRjoAQA, right side
 * @author pit
 *
 */
public class RightSideClashTest extends AbstractClasspathResolvingTest {
	private boolean verbose = false;

	@Override
	protected RepoletContent archiveInput() {	
		File file = new File( input, "cyclingClashingTree.txt");
		try {
			return RepositoryGenerations.parseConfigurationFile( file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot parse file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 
	}	
	@Test
	public void testRightSideFirstOccurrence() {
		ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build() //
			.clashResolvingStrategy(ClashResolvingStrategy.firstOccurrence) // 
			.lenient(false) // 
			.scope(ClasspathResolutionScope.compile) //
			.done(); //
		
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#[1.0,1.1)", resolutionContext, "right-side");
		
		if (verbose) {
			System.out.println("*** first occurence ***");
			for (AnalysisArtifact artifact: resolution.getSolutions()) {
				System.out.println(artifact.asString());
				
				getCpJarParts(artifact).forEach(p -> System.out.println(" - " + ((FileResource)p.getResource()).getPath())); 
			}
		}
		
		Validator validator = new Validator();
		validator.validateExpressive( new File( input, "cyclingClashingTree.validation.txt"), resolution);
		validator.assertResults();
	}
	
	@Test
	public void testRightSideHighestVersion() {
		ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build() //
				.clashResolvingStrategy(ClashResolvingStrategy.highestVersion) // 
				.lenient(false) // 
				.scope(ClasspathResolutionScope.compile) //
				.done(); //
		
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#[1.0,1.1)", resolutionContext, "right-side-highest");
		
		if (verbose) {
			System.out.println("*** highest version ***");
			for (AnalysisArtifact artifact: resolution.getSolutions()) {
				System.out.println(artifact.asString());
				
				getCpJarParts(artifact).forEach(p -> System.out.println(" - " + ((FileResource)p.getResource()).getPath())); 
			}
		}
		
		Validator validator = new Validator();
		validator.validateExpressive( new File( input, "cyclingClashingTree.validation.txt"), resolution);
		validator.assertResults();
	}
}
