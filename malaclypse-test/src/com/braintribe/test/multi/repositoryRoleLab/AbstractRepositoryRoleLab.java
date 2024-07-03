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
package com.braintribe.test.multi.repositoryRoleLab;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifact.test.repolet.Repolet;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.AbstractWalkLab;
import com.braintribe.test.multi.ClashStyle;
import com.braintribe.test.multi.WalkDenotationTypeExpert;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public abstract class AbstractRepositoryRoleLab extends AbstractWalkLab {
	private static LauncherShell launcherShell;
	private static File localRepository = new File ("res/repositoryRoleLab/contents/repo");
	private static File contents = new File( "res/repositoryRoleLab/contents");
	private static final File [] data = new File[] { new File( contents, "archiveBase.zip"),
													 new File( contents, "archiveRelease.zip"), 
													 new File( contents, "archiveSnapshot.zip"),													 
	};

	@SuppressWarnings("unused")
	private static List<Repolet> launchedRepolets;	
	
	protected abstract String [] getResultsForRun();


	protected static void before( File settings) {
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		localRepository.mkdirs();
		int port = runBefore();
		
		// clean local repository
		TestUtil.delete(localRepository);
			
		// fire them up 
		launchRepolets( port);
	}

	private static void launchRepolets( int port) {
		String [] args = new String[1];
		args[0] = 	"archive," + data[0].getAbsolutePath() +
					";release," + data[1].getAbsolutePath() + 
					";snapshot," + data[2].getAbsolutePath();
		launcherShell = new LauncherShell( port);
		launchedRepolets = launcherShell.launch( args, RepoType.singleZip);
	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}
	
	@Test
	public void testRepositoryRole() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.repositoryRoleTest");
			terminal.setArtifactId( "RepositoryRoleTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			 
			String[] expectedNames = getResultsForRun();
			
			Collection<Solution> result = test( "testRepositoryRole", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
						
						
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	
	
}
