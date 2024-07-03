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
package com.braintribe.test.multi.providedScopeLab;

import java.io.File;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;


/**
 *
 * 
 * @author pit
 *
 */
public class ProvidedScopeTestLab extends AbstractProvidedScopeLab {
		
	
	protected static File settings = new File( "res/providedScope/contents/settings.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testCompile() {
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test:provided-scope-test-a#1.0.1-pc",
				"com.braintribe.devrock.test:provided-scope-test-b#1.0.1-pc",
				"com.braintribe.devrock.test:provided-scope-test-provided#1.0.1-pc",								
		};
		
		runTest( "com.braintribe.devrock.test:provided-scope-test-terminal#1.0.1-pc", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	@Test
	public void testRuntime() {
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test:provided-scope-test-a#1.0.1-pc",
				"com.braintribe.devrock.test:provided-scope-test-b#1.0.1-pc",										
		};
		
		runTest( "com.braintribe.devrock.test:provided-scope-test-terminal#1.0.1-pc", expectedNames, ScopeKind.launch, WalkKind.classpath, false);
	}
	
	
}
