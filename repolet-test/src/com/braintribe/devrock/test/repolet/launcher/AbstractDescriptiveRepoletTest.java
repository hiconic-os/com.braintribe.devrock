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
