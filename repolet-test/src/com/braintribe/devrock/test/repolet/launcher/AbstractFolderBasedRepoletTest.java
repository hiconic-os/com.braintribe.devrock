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
