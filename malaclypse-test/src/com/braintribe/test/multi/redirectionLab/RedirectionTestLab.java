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
package com.braintribe.test.multi.redirectionLab;

import java.io.File;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Solution;


/**
 * 
 * RedirectionTerminal#1.0 
	A#1.0 -> redirects to C
	B#1.0 -> redirects to D
	
 * @author pit
 *
 */
public class RedirectionTestLab extends AbstractRedirectionLab {
		
	
	protected static File settings = new File( contents, "settings.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testCompileOptionalIncluded() {
		String[] expectedNames = new String [] {											
				"com.braintribe.test.dependencies.redirectionTest:C#1.0",			
				"com.braintribe.test.dependencies.redirectionTest:D#1.0",
				"com.braintribe.test.dependencies.redirectionTest:E#1.0",
		};
		
		runTest( "com.braintribe.test.dependencies.redirectionTest:RedirectionTestTerminal#1.0", expectedNames);
	}
	
	
	

	
}
