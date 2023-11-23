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
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests a case where the same artifact has two references with differing classifiers.
 * 
 * @author pit
 *
 */
public class SelfReferenceWithDifferentClassifiersTest extends AbstractClasspathResolvingTest {
	@Override
	protected RepoletContent archiveInput() {		
		return loadInput( new File( input, "classifier.selfreference.definition.yaml"));
	}

	@Test 
	public void runSelfReferenceWithClassifiersTest() {
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().scope(ClasspathResolutionScope.compile).done(), true, "failing-selfreference-with-differing-classifiers");
		
		/*
		Validator validator = new Validator();		
		validator.assertTrue("resolution should have failed, but didn't", resolution.hasFailed());						
		validator.assertResults();
		*/
		
	}

}
