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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.ClashResolvingStrategy;
import com.braintribe.testing.category.KnownIssue;

/**
 * tests that clashing still works if the two dependencies (each with different classifier) still leads to a 
 * clash if the owning artifact's different (differing versions)
 * 
 * clash on c#1.0.1, c#1.0.2. Both c# do need contain the same classified jar as the one looses, the new one is taken which 
 * replaces the old one - and needs to have same parts. In this test, they have differing classifiers, so one part cannot be 
 * resolved.
 * 
 * This tests the correct reporting of a missing part (due to clashing - classifier) 
 *  
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class ClashesWithUnresolvedClassifiersTest extends AbstractClasspathResolvingTest {
	private Map<String,List<String>> replacementMap = new HashMap<>();	
	{
		List<String> losersForC = new ArrayList<>();
		losersForC.add( "com.braintribe.devrock.test:c#1.0.1");		
		replacementMap.put( "com.braintribe.devrock.test:c#1.0.2", losersForC);		
	}
	
	private List<String> expectedIncompleteArtifacts = new ArrayList<>();
	{
		expectedIncompleteArtifacts.add( "com.braintribe.devrock.test:c#1.0.2");
	}
	

	@Override
	protected RepoletContent archiveInput() {	
		File file = new File( input, "simpleClashingWithUnresolvedClassifiersTree.definition.txt");
		try {
			return RepositoryGenerations.parseConfigurationFile( file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot parse file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 
	}	
	
	
	
	@Test
	public void runClashesWithUnresolvedClassifiersTest() {
		ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build() //
			.clashResolvingStrategy(ClashResolvingStrategy.firstOccurrence) // 
			.lenient(true) // 
			.scope(ClasspathResolutionScope.compile) //
			.done(); //
		
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#[1.0,1.1)", resolutionContext, "unresolved-classifiers");
		
		Validator validator = new Validator();
		// validate result 
		validator.validateExpressive( new File( input, "simpleClashingWithClassifiersTree.validation.txt"), resolution);
		
		// validate clashes 
		validator.validateClashes(resolution, replacementMap);
						
		// validate that no error's returned 
		validator.validateFailedResolution(resolution, expectedIncompleteArtifacts, null);
				
		validator.assertResults();
	}

}
