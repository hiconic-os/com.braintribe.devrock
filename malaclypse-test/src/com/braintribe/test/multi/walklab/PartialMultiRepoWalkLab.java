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
import org.junit.Assert;
import org.junit.BeforeClass;

import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;

/**
 * tests walks over multiple repositories that are disjunct, i.e. the repositories contain a different set of artifacts
 *  
 * 
 * @author pit
 *
 */
public class PartialMultiRepoWalkLab extends AbstractMultiRepoWalkLab {
	private static LauncherShell launcherShell;
	private static File settings = new File( "res/walklab/contents/partial/settings.user.xml");
	private static File localRepository = new File ("res/walklab/contents/partial/repo");
	private static File contents = new File( "res/walklab/contents/partial");
	private static final File [] data = new File[] { new File( contents, "archiveA.zip"), 
													 new File( contents, "archiveB.zip"),
													 new File( contents, "archiveC.zip"),
													 new File( contents, "archiveD.zip"),
	};


	@BeforeClass
	public static void before() {
		int port = before( settings, localRepository);
		runBefore();
		
			
		// fire them up 
		launchRepolets( port);
		 
	}

	private static void launchRepolets(int port) {
		String [] args = new String[1];
		args[0] = 	"archiveA," + data[0].getAbsolutePath() + 
					";archiveB," + data[1].getAbsolutePath() + 
					";archiveC," + data[2].getAbsolutePath() + 
					";archiveD," + data[3].getAbsolutePath();
					
		// unpack last one
		
		try {
			Archives.zip().from( data[3]).unpack( localRepository).close();
		} catch (ArchivesException e) {
			Assert.fail("cannot unpack archive [" + data[3].getAbsolutePath() + "] to [" + localRepository.getAbsolutePath() + "]");
		}
		
		launcherShell = new LauncherShell( port);
		launcherShell.launch(args, RepoType.singleZip);		
	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}	
	
}
