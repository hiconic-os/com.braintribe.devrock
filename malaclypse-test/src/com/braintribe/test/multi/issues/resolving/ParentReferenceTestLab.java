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
package com.braintribe.test.multi.issues.resolving;

import java.io.File;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;
import com.braintribe.testing.category.KnownIssue;


/**
 
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class ParentReferenceTestLab extends AbstractResolvingIssueLab {
		
	
	protected static File settings = new File( contents, "settings.datapedia.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	//@Test
	public void testRange() {
		String[] expectedNames = new String [] {					
				
		};
		
		runTest( "com.tribefire.ignition.googlecloud.storage:google-cloud-storage-deployable-experts#1.0.2", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	
	

	
}
