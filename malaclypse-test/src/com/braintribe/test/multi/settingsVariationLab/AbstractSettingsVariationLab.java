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
package com.braintribe.test.multi.settingsVariationLab;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;

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

public abstract class AbstractSettingsVariationLab extends AbstractWalkLab {
	private static LauncherShell launcherShell;
	protected static File contents = new File( "res/settingsVariationLab/contents");
	private static File localRepository = new File ( contents, "repo");
	private static final File [] data = new File[] { new File( contents, "archiveBase.zip"),
													 new File( contents, "archiveRelease.zip"),														
	};

	@SuppressWarnings("unused")
	private static List<Repolet> launchedRepolets;	
	
	protected abstract String [] getResultsForRun();


	protected static void before( File settings) {
		if (settings != null) {
			settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
			localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		}
		int port = runBefore();
		
		// clean local repository
		TestUtil.ensure(localRepository);
			
		// fire them up 
		launchRepolets( port);
	}

	private static void launchRepolets( int port) {
		String [] args = new String[1];
		args[0] = 	"archive," + data[0].getAbsolutePath() +
					";release," + data[1].getAbsolutePath();
		launcherShell = new LauncherShell( port);
		launchedRepolets = launcherShell.launch( args, RepoType.singleZip);
	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}
	
	//@Test
	public void testSettings() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.repositoryRoleTest");
			terminal.setArtifactId( "RepositoryRoleTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			 
			String[] expectedNames = getResultsForRun();
			
			Collection<Solution> result = test( "settingsVariations", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
						
						
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	
	
}
