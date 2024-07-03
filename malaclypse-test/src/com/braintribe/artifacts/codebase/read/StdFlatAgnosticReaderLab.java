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
package com.braintribe.artifacts.codebase.read;

import java.io.File;

import org.junit.BeforeClass;

public class StdFlatAgnosticReaderLab extends AbstractLocationAgnosticReaderLab {
	
	@BeforeClass
	public static void runbefore() {
		target = new File( contents, "standard.expanded");
		targetGrpOneA = new File( target, "com.braintribe.grpOne/A/1.0/pom.xml");
		targetGrpOneB = new File( target, "com.braintribe.grpOne/B/1.0/pom.xml");
		targetGrpOneSubOneA = new File( target, "com.braintribe.grpOne.subOne/A/1.0/pom.xml");
		
		setupCodebase( FLATTENED_STANDARD);
		AbstractLocationAgnosticReaderLab.runbefore();
	}

}
