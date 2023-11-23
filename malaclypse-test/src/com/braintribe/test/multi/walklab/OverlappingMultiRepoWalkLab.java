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
