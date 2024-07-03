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
package com.braintribe.test.multi.mavendiscrepancy;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.braintribe.test.multi.ClashStyle;
import com.braintribe.testing.category.KnownIssue;

/**
 * NOTE : A#1.0 is taken as its only reference is in B#1.0 which is taken over B#1.1, hence the request to 
 * A#1.1 is weeded. 
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class MavenDiscrepancyOptimisticLab extends AbstractMavenDiscrepancyLab {	
	protected static File settings = new File( "res/mavenDiscrepancyLab/contents/settings.mavenDiscrepancy.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}

	//@Test 
	public void optimistic() {
		String [] optimistic = new String [] { "com.braintribe.test.dependencies.mavenDiscrepancyTest:A#1.0", 
												"com.braintribe.test.dependencies.mavenDiscrepancyTest:B#1.1",
												"com.braintribe.test.dependencies.mavenDiscrepancyTest:C#1.1",
		};
		runTest( optimistic, ClashStyle.optimistic);
	}
			
}
