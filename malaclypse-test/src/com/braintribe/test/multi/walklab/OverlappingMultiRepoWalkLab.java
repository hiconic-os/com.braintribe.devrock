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
package com.braintribe.test.multi.walklab;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;

/**
 * tests walks over multiple repositories, the artifacts are overlapping, i.e. some artifacts are existing in several repositories, yet
 * different solutions (basically one repository contains the never versions in this example) 
 * 
 * @author pit
 *
 */
public class OverlappingMultiRepoWalkLab extends AbstractMultiRepoWalkLab {
	private static LauncherShell launcherShell;
	private static File settings = new File( "res/walklab/contents/overlap/settings.user.xml");
	private static File localRepository = new File ("res/walklab/contents/overlap/repo");
	private static File contents = new File( "res/walklab/contents/overlap");
	private static final File [] data = new File[] { new File( contents, "archiveBase.zip"), 
													 new File( contents, "archiveAddOn.zip"),													 
	};


	@BeforeClass
	public static void before() {
		
		int port = before( settings, localRepository);	
		// fire them up 
		launchRepolets( port);				
		
	}

	private static void launchRepolets( int port) {
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
	
	
}
