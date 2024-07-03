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

import com.braintribe.devrock.repolet.launcher.builder.api.LauncherCfgBuilderContext;

public abstract class AbstractFolderBasedRepoletTest extends AbstractLauncherTest {

	{
		launcher = LauncherCfgBuilderContext.build()
				.repolet()
					.name("archiveA")
					.changesUrl("http://localhost:${port}/archiveA/rest/changes")
					.changes( dateCodec.decode(testDate1AsString), new File( root, "setup/remoteRepoA/rh.answer.1.txt"))
					.changes( dateCodec.decode(testDate2AsString), new File( root, "setup/remoteRepoA/rh.answer.2.txt"))
					.changes( dateCodec.decode(testDate3AsString), new File( root, "setup/remoteRepoA/rh.answer.3.txt"))
					.serverIdentification("artifactory")
					.restApiUrl("http://localhost:${port}/api/storage/archiveA")
					.filesystem()
						.filesystem( new File( root, "contents/remoteRepoA"))
					.close()
				.close()
				.repolet()
					.name("archiveB")
					.changesUrl("http://localhost:${port}/archiveB/rest/changes")
					.changes( dateCodec.decode(testDate1AsString), new File( root, "setup/remoteRepoB/rh.answer.1.txt"))
					.changes( dateCodec.decode(testDate2AsString), new File( root, "setup/remoteRepoB/rh.answer.2.txt"))
					.changes( dateCodec.decode(testDate3AsString), new File( root, "setup/remoteRepoB/rh.answer.3.txt"))
					.serverIdentification("artifactory")
					.restApiUrl("http://localhost:${port}/api/storage/archiveB")
					.filesystem()
						.filesystem( new File( root, "contents/remoteRepoB"))
					.close()
				.close()
			.done();				
	}
}
