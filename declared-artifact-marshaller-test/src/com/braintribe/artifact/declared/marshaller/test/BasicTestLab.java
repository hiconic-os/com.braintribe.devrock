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
package com.braintribe.artifact.declared.marshaller.test;

import java.io.File;

import org.junit.Test;

import com.braintribe.model.artifact.declared.DeclaredArtifact;

public class BasicTestLab extends AbstractDeclaredArtifactMarshallerLab {

	@Test
	public void test() {
		File file = new File( input, "simple.xml");
		File comparison = new File( input, "simple.cmp.xml");

		DeclaredArtifact artifact = read( file);
		validate( artifact, comparison);
	}
	
	@Test
	public void cartridgeTest() {
		File file = new File( input, "cartridge.xml");
		File comparison = new File( input, "cartridge.cmp.xml");

		DeclaredArtifact artifact = read( file);
		validate( artifact, comparison);
	}

}
