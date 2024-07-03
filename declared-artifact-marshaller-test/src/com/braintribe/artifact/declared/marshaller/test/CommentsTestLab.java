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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.declared.DeclaredDependency;
import com.braintribe.model.artifact.declared.ProcessingInstruction;

public class CommentsTestLab extends AbstractDeclaredArtifactMarshallerLab {

	//@Test
	public void testSimpleComments() {
		File file = new File( input, "comments.xml");		
		DeclaredArtifact artifact = read( file);	
		
		List<DeclaredDependency> dependencies = artifact.getDependencies();
		for (DeclaredDependency dependency : dependencies) {
			List<ProcessingInstruction> processingInstructions = dependency.getProcessingInstructions();
			int size = processingInstructions.size();
			Assert.assertTrue("expected to find processing instructions, yet didn't", size > 0);
			Assert.assertTrue("expected to find exactly one processing instructions, yet found [" + size + "]", size == 1);
			
			ProcessingInstruction pi = processingInstructions.get(0);
			Assert.assertTrue("expected target to be [tag], yet found [" + pi.getTarget() + "]", pi.getTarget().equals("tag"));
			Assert.assertTrue("expected data to be [asset], yet found [" + pi.getData() + "]", pi.getData().equals("asset"));
		
		}

	}
}
