// ============================================================================
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
package com.braintribe.test.multi.repo.enricher;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.SolutionListPresence;
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
public class EnricherLenientAndTrustworthyTestLab extends AbstractEnricherRepoLab {
		
	
	protected static File settings = new File( "res/enricherLab/contents/settings.listing-lenient.trustworthy.xml");
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
	
	@Test
	public void testLenientAndTrustworthy() {
		String[] expectedNames = new String [] {						
				group + ":" + "a" + "#" + version, 
				group + ":" + "b" + "#" + version, 
		};
		
		// first run 
		runTest( null, group + ":" + "lenient-test-terminal-one" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
		
		// second run 
		runTest( null, group + ":" + "lenient-test-terminal-one" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
		
		validateIndices("braintribe.Base", expectedNames[0], "sources.jar", ".jar", ".pom", ".md5", ".sha1", "xml");
		validateIndices("braintribe.Base", expectedNames[1], "sources.jar", ".jar", ".pom", ".md5", ".sha1", "xml");
	}
	
	protected void validateStatus( String name, SolutionListPresence presence) {
		if (presence != SolutionListPresence.present) {
			Assert.fail( "file [" + name + "] has status [" + presence + "]");
		}	
	}
	

}
