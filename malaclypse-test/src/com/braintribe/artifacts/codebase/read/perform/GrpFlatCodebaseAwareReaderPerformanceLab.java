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
package com.braintribe.artifacts.codebase.read.perform;

import java.io.File;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.braintribe.model.artifact.Solution;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class GrpFlatCodebaseAwareReaderPerformanceLab extends AbstractCodebaseAwareReaderPerformanceLab {

	@BeforeClass
	public static void runBefore() {		
		masterCodebase = new File( System.getenv( "BT__ARTIFACTS_HOME"), "../artifact-groups" );		
		runbefore( FLATTENED_GROUP);
	}
	
	//@Test
	public void gaugeMalaclypseSingle() {
		Solution solution = singleRead( new File( masterCodebase, "com.braintribe.devrock/1.0/Malaclypse/pom.xml"));
		Assert.assertNotNull(solution);
	}

	//@Test
	public void gaugeMalaclypseMultiple() {
		Solution solution = multipleRead( new File( masterCodebase, "com.braintribe.devrock/1.0/Malaclypse/pom.xml"), 110, 10);		
		Assert.assertNotNull(solution);
	}
}
