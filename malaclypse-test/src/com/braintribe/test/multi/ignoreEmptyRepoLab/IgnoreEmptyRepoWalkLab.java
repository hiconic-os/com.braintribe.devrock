// ============================================================================
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
package com.braintribe.test.multi.ignoreEmptyRepoLab;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.model.artifact.Artifact;

/**
 * tests walks over multiple repositories, where artifacts are not complete, but parts (the sources for instance) exist in different artifacts 
 * 
 * @author pit
 *
 */
public class IgnoreEmptyRepoWalkLab extends AbstractIgnoreEmptyRepoLab {
	private static File contents = new File( "res/ignoreEmptyRepositories/contents");
	private static File settings = new File( contents, "settings.user.xml");
	private static File localRepository = new File (contents, "repo");
	
	private static final File [] data = new File[] { new File( contents, "archive.base.zip"), 
													 new File( contents, "archive.add.zip"),													 
	};
	private static LauncherShell launcherShell;


	@BeforeClass
	public static void before() {
		int port = before(settings, localRepository);
		launchRepolets( port);
	}

	protected static void launchRepolets( int port) {
		String [] args = new String[1];
		args[0] = 	"archiveBase," + data[0].getAbsolutePath() + 
					";archiveAddOn," + data[1].getAbsolutePath();				
		launcherShell = new LauncherShell( port);
		launcherShell.launch(args,  RepoType.singleZip);
	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}
	
	@Override
	protected void testCollateralFiles(File localRepository, Map<String, List<String>> names) {
		for (Entry<String, List<String>> entry : names.entrySet()) {
			for (String artifactName : entry.getValue()) {
				testCollatoralFiles(localRepository, NameParser.parseCondensedArtifactName(artifactName), entry.getKey());
			}
		}
	}
	
	private boolean testCollatoralFiles( File localRepository, Artifact artifact, String valid) {
		File localArtifactDirectory = RepositoryReflectionHelper.getFilesystemLocation(localRepository, artifact);
		if (!localArtifactDirectory.exists()) {
			return false;
		}
		boolean retval = true;
		for (File file : localArtifactDirectory.listFiles()) {
			String name = file.getName();
			if (name.endsWith( ".solution")) {
				if (!name.equalsIgnoreCase( valid + ".solution")) {
					Assert.fail( "file [" + file.getAbsolutePath() + "] is not supposed to be there");
					retval = false;
				}
			}
			else if (name.startsWith( "maven-metadata-")) {
				if (!name.equalsIgnoreCase( "maven-metadata-" + valid + ".xml")) {
					Assert.fail( "file [" + file.getAbsolutePath() + "] is not supposed to be there");
					retval = false;
				}
			}
		}
		return retval;
		
	}
	

	
}
