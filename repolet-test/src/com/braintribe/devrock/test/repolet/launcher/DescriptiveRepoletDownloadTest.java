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
package com.braintribe.devrock.test.repolet.launcher;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.resource.FileResource;

public class DescriptiveRepoletDownloadTest extends AbstractDescriptiveRepoletTest {

	@Override
	protected RepoletContent getContent() {
		RepoletContent content = RepoletContent.T.create();
		
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId("com.braintribe.devrock.test");
		artifact.setArtifactId("t");
		artifact.setVersion( "1.0.1");
		
		File file = new File( contents, "content.txt");
		FileResource resource = FileResource.T.create();
		resource.setPath( file.getAbsolutePath());
		
		artifact.getParts().put("jar", resource);
		
		content.getArtifacts().add(artifact);
		
		return content;
		
	}

	@Before
	public void before() {
		runBeforeBefore();
		launcher.launch();
	}
	
	@After
	public void after() {
		launcher.shutdown();
	}
	
	//@Test
	public void run() {
		System.out.println("This needs to be the breakpoint");
	}
	
	
	
}
