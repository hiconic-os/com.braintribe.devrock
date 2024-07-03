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
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.declared.DeclaredGroupExtractionContext;
import com.braintribe.model.artifact.declared.DeclaredGroup;

/**
 * @author pit
 *
 */
public class DeclaredGroupExtractionTest extends AbstractGroupExtractionTest {
	private static final String RANGE_STANDARD = "[1.0,1,1)";
		
	/**
	 * standard extraction with a non-matching exclusion
	 */
	@Test
	public void runBasicExtraction() {
		File testGroup = new File( input, "com.braintribe.devrock.test.grp1");
		
		Map<String, String> expectations = new HashMap<>();
		expectations.put( "com.braintribe.devrock.test.grp1", RANGE_STANDARD);
		expectations.put( "com.braintribe.devrock.test.grp2", RANGE_STANDARD);
		expectations.put( "com.braintribe.devrock.test.grp3", RANGE_STANDARD);
		expectations.put( "com.braintribe.devrock.test.grpX", RANGE_STANDARD);
		
		
		DeclaredGroupExtractionContext context = DeclaredGroupExtractionContext.build()
														.location( testGroup)
														.includeMembers(false)
														.includeParent(false)
														.includeSelfreferences(true)
														.sort(true)
														.exclusions("not.my.group.*")														
												.done();		
		DeclaredGroup declaredGroup = runGroupExtractionLab( context);
		
		validate(declaredGroup, expectations);		
	}
	
	/**
	 * test with inclusion and exclusion filters
	 */
	@Test
	public void runFilteredExtraction() {
		File testGroup = new File( input, "com.braintribe.devrock.test.grp1");
		
		Map<String, String> expectations = new HashMap<>();
		expectations.put( "com.braintribe.devrock.test.grp1", RANGE_STANDARD);
		expectations.put( "com.braintribe.devrock.test.grp2", RANGE_STANDARD);				
		
		DeclaredGroupExtractionContext context = DeclaredGroupExtractionContext.build()
														.location( testGroup)
														.includeMembers(false)
														.includeParent(false)
														.includeSelfreferences(true)
														.sort(true)
														.exclusions("com.braintribe.devrock.test.grpX")
														.inclusions( "com.braintribe.devrock.test.grp1")
														.inclusions( "com.braintribe.devrock.test.grp2")
												.done();		
		DeclaredGroup declaredGroup = runGroupExtractionLab( context);
		
		validate(declaredGroup, expectations);		
	}
	
	@Test
	public void runExtractionWithoutSelfreference() {
		File testGroup = new File( input, "com.braintribe.devrock.test.grp1");
		
		Map<String, String> expectations = new HashMap<>();
		expectations.put( "com.braintribe.devrock.test.grp2", RANGE_STANDARD);				
		
		DeclaredGroupExtractionContext context = DeclaredGroupExtractionContext.build()
														.location( testGroup)
														.includeMembers(false)
														.includeParent(false)
														.sort(true)
														.includeSelfreferences(false)
														.exclusions("com.braintribe.devrock.test.grpX")
														.inclusions( "com.braintribe.devrock.test.grp1")
														.inclusions( "com.braintribe.devrock.test.grp2")
												.done();		
		DeclaredGroup declaredGroup = runGroupExtractionLab( context);
		
		validate(declaredGroup, expectations);		
	}
	
	
	@Test
	public void runIncompleteExtraction() {
		File testGroup = new File( input, "com.braintribe.devrock.test.grp2");
		
	
		DeclaredGroupExtractionContext context = DeclaredGroupExtractionContext.build()
														.location( testGroup)
														.includeMembers(false)
														.includeParent(false)														
												.done();		
		DeclaredGroup declaredGroup = runGroupExtractionLab( context);
		
		Assert.assertTrue("unexceptedly, the result wasn't flagged as invalid", declaredGroup.getFailure() != null);
			
	}
	
}
