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

import org.junit.experimental.categories.Category;

import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.launcher.builder.api.LauncherCfgBuilderContext;
import com.braintribe.devrock.test.repolet.launcher.utils.TestUtils;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public abstract class AbstractDescriptiveRepoletTest extends AbstractLauncherTest {
	protected static final String changesUrlHeader = "X-Artifact-Repository-Changes-Url";
	protected static final String serverHeader = "Server";
	
	protected File contents = new File( "res/descriptive");	
	protected File uploads = new File( contents, "uploads");
	protected File uploaded = new File( contents, "uploaded");
	
	protected void runBeforeBefore() {
		TestUtils.ensure(uploaded);
		
		launcher = LauncherCfgBuilderContext.build()
			.repolet()
				.name("archive")
				.descriptiveContent()
					.descriptiveContent( getContent())
				.close()
				.serverIdentification("artifactory")
				.restApiUrl("http://localhost:${port}/api/storage/archive")
				.changesUrl("http://localhost:${port}/archive/rest/changes")
				.uploadFilesystem()
					.filesystem( uploaded)
				.close()
			.close()
		.done();
	}
	

	
	@Override
	protected File getRoot() {
		return null;
	}
	
	protected abstract RepoletContent getContent();



}
