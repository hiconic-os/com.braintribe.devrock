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
