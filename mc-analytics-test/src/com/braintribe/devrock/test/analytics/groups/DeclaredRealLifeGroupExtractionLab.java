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
package com.braintribe.devrock.test.analytics.groups;

import java.io.File;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.mc.api.declared.DeclaredGroupExtractionContext;
import com.braintribe.model.artifact.declared.DeclaredGroup;
import com.braintribe.testing.category.KnownIssue;

/**
 * simple lab to run some group analysis tests
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class DeclaredRealLifeGroupExtractionLab extends AbstractGroupExtractionTest {
	private File root = new File( "f:/works/dev-envs/standard/git");

	@Test
	public void runExtraction_ComBraintribeCommon() {
		File testGroup = new File( root, "com.braintribe.common");
		
		DeclaredGroupExtractionContext context = DeclaredGroupExtractionContext.build()
														.location( testGroup)
														.includeMembers(false)
														.includeParent(false)
														.enforceRanges(true)
														.sort(true)
														.inclusions("com.braintribe.*")
														.inclusions("tribefire.*")
												.done();		
		DeclaredGroup declaredGroup = runGroupExtractionLab( context);
		System.out.println();
	}
	
	@Test
	public void runExtraction_TribefireCortex() {
		File testGroup = new File( root, "tribefire.cortex");
		
		DeclaredGroupExtractionContext context = DeclaredGroupExtractionContext.build()
														.location( testGroup)
														.includeMembers(false)
														.includeParent(false)
														.sort(true)
														.simplifyRange(true)
														/*
														.exclusions("javax.*")
														.exclusions("org.*")
														.exclusions("io.*")
														*/
														.inclusions("com.braintribe.*")
														.inclusions("tribefire.*")
														.enforceRanges( true)
												.done();		
		DeclaredGroup declaredGroup = runGroupExtractionLab(context);
		System.out.println();
	}
}
