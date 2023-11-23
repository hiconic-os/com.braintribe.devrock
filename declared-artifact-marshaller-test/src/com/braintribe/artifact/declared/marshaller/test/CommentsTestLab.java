// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
