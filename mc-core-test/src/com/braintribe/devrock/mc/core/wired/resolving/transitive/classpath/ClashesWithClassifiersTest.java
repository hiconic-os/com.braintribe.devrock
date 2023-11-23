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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * tests that clashing still works if the two dependencies (each with different classifier) still leads to a 
 * clash if the owning artifact's different (differing versions)
 * 
 * clash on c#1.0.1, c#1.0.2. Both c# do need contain the same classified jar as the one looses, the new one is taken which 
 * replaces the old one - and needs to have same parts.
 * @author pit
 *
 */
public class ClashesWithClassifiersTest extends AbstractClasspathResolvingTest {
	private Map<String,List<String>> replacementMap = new HashMap<>();	
	{
		List<String> losersForC = new ArrayList<>();
		losersForC.add( "com.braintribe.devrock.test:c#1.0.1");		
		replacementMap.put( "com.braintribe.devrock.test:c#1.0.2", losersForC);		
	}

	@Override
	protected RepoletContent archiveInput() {	
		File file = new File( input, "simpleClashingWithClassifiersTree.definition.txt");
		try {
			return RepositoryGenerations.parseConfigurationFile( file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot parse file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 
	}	
	
	
	
	@Test
	public void run() {
		ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build() //
			.clashResolvingStrategy(ClashResolvingStrategy.firstOccurrence) // 
			.lenient(true) // 
			.scope(ClasspathResolutionScope.compile) //
			.done(); //
		
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#[1.0,1.1)", resolutionContext, "classifiers");
		
		Validator validator = new Validator();
		
		// validate result 
		validator.validateExpressive( new File( input, "simpleClashingWithClassifiersTree.validation.txt"), resolution);
		
		// validate clashes 
		validator.validateClashes(resolution, replacementMap);
						
		// validate that no error's returned 
		validator.validateFailedResolution(resolution, null, null);
				
		validator.assertResults();
	}

}
