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
package com.braintribe.artifacts.test.maven.pom.marshall;

import org.junit.Test;

import com.braintribe.model.artifact.Solution;

/**
 * test exclusions
 * 
 * @author pit
 *
 */
public class TroubleShooterLab extends AbstractPomMarshallerTest {
	
	
	@Override
	public boolean validate(Solution solution) {
	
		return true;
	
	
	}
	
	
	@Test
	public void testTCC() {
		for (int i = 0; i < 10000; i++) 
			read( "TribefireControlCenter-2.0.pom");
	}
	
	@Test
	public void junit412() {
		read( "junit-4.12.pom");
	}
	

}
