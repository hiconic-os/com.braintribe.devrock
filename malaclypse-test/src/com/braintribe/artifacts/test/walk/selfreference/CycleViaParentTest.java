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
package com.braintribe.artifacts.test.walk.selfreference;

import java.io.File;

import org.junit.Test;


public class CycleViaParentTest extends AbstractSelfReferenceTest {
	private static final String TERMINAL = "com.braintribe.devrock.test:terminal#1.0.1";	
	
	@Override
	protected File getTestArchiveContent() {		
		return new File( input, "cycleViaParent");
	}

	@Test
	public void classpathTest() {
		classpathResolutionTest(TERMINAL, true);
	}
		
	@Test
	public void buildTest() {
		buildPathResolutionTest(TERMINAL, true);
	}

}
