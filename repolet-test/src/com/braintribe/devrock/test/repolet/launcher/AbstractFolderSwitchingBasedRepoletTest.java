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

public abstract class AbstractFolderSwitchingBasedRepoletTest extends AbstractLauncherTest {

	{
		launcher = LauncherCfgBuilderContext.build()
				.repolet()
					.name("archive")
					.changesUrl("http://localhost:${port}/archive/rest/changes")
					.indexedFilesystem()
						.initialIndex("one")
						.filesystem( "one", new File( root, "contents/remoteRepoA"))
						.filesystem( "two", new File( root, "contents/remoteRepoB"))
					.close()
				.close()				
			.done();				
	}
}
