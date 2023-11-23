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
package com.braintribe.test.multi.repo.trustworthyLab;

import java.io.File;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.ClashStyle;
import com.braintribe.test.multi.WalkDenotationTypeExpert;

/**
 * tests the feature to handle lying repositories (aka their maven-metadata.xml is wrong)
 * 
 *   this now tests a repository that can be trusted 
 * 
 * @author pit
 *
 */
public class TrustworthyMultiRepoWalkLab extends AbstractTrustworthyMultiRepoWalkLab {
	private static File contents = new File( "res/trustworthyRepositoryLab/contents");
	private static File base = new File( contents, "base");	
	
	private static File settings = new File( contents, "settings.trustworthy.xml");
	private static File localRepository = new File ( contents, "repo");
	
	private static final File [] data = new File[] { new File( contents, "archiveA.zip")};
	private static LauncherShell launcherShell;


	@BeforeClass
	public static void before() {
		int port = before(settings, localRepository);
		launchRepolets( port);
		//copy base to repo 
		TestUtil.copy(base, localRepository);
	}

	protected static void launchRepolets( int port) {
		String [] args = new String[1];
		args[0] = 	"archiveA," + data[0].getAbsolutePath();				
		launcherShell = new LauncherShell( port);
		launcherShell.launch(args,  RepoType.singleZip);
	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}
	

	@Test
	public void testUnworthyRepository() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.updatePolicyTest");
			terminal.setArtifactId( "UpdatePolicyTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			
			// only archiveA's allowed to be relevant, so  
			String[] expectedNames = new String [] {								
					"com.braintribe.test.dependencies.updatePolicyTest:B#1.0",									
			};
			
			Collection<Solution> result = test( "testupdate", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
			
		}
		catch( Exception e) {
			Assert.fail("exception [" + e.getMessage() + "] thrown");
		}
	}
	
}
