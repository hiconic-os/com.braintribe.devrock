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
package com.braintribe.test.multi.repo.lenient;

import java.io.File;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;


/**
 *
 * 
 * 
 *
 * @author pit
 *
 */
public class LenientRepoTestLab extends AbstractLenientRepoLab {
		
	
	protected static File settings = new File( "res/lenientRepo/contents/settings.xml");
	protected static String group = "com.braintribe.devrock.test.lenient";
	protected static String version = "1.0";
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	
	public void testNonLenientYetTrustworthy() {
		String[] expectedNames = new String [] {						
				group + ":" + "a" + "#" + version, // no packaging -> jar
				group + ":" + "b" + "#" + version, // jar packaging
		};
		
		// first run 
		runTest( null, group + ":" + "lenient-test-terminal-one" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
		
		// second run 
		runTest( null, group + ":" + "lenient-test-terminal-one" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
		
}
