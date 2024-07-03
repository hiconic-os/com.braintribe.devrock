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
