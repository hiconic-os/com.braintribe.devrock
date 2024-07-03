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
package com.braintribe.artifacts.codebase.walk.perform;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;
@Category(KnownIssue.class)
public class GrpFlatCodebaseAwareWalkPerformanceLab extends AbstractCodebaseAwareWalkPerformanceLab {
	
	@BeforeClass
	public static void runBefore() {		
		masterCodebase = new File( System.getenv( "BT__ARTIFACTS_HOME"), "../artifact-groups" );
		runbefore( FLATTENED_GROUP);
	}

	
	//@Test
	public void testMalaclypseSingle() {
		File file = new File( masterCodebase, "com.braintribe.devrock/1.0/Malaclypse/pom.xml");
		walk( file, null);
	}
	
	//@Test
	public void testMalaclypseMultiple() {
		File file = new File( masterCodebase, "com.braintribe.devrock/1.0/Malaclypse/pom.xml");
		walk( file, null, 110, 10);
	}
}
