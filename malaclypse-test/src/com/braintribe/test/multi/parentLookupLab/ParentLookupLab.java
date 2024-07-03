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
package com.braintribe.test.multi.parentLookupLab;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.AbstractWalkLab;
import com.braintribe.test.multi.ClashStyle;
import com.braintribe.test.multi.WalkDenotationTypeExpert;

/**
 * testing : a) parent lookup, b) dependency management, c) property resolving
 * <br/>
 * Terminal
 * 	-> parent : Parent#1.0
 * 		-> dep mgt : ParentImport#1.0 
 * 			-> dep mgt : ParentImportImport#1.0
 * 				-> parent : PropertyParent (variable decl)
 * -> A (?)
 * -> B (?)
 * -> C (?)
 * @author pit
 *
 */
public  class ParentLookupLab extends AbstractWalkLab {
	private static LauncherShell launcherShell;
	private static File localRepository = new File ("res/parentLookupTest/contents/repo");
	private static File contents = new File( "res/parentLookupTest/contents");
	private static final File [] data = new File[] { new File( contents, "archive.zip"),
													 												 
	};
	
	protected static File settings = new File( "res/parentLookupTest/contents/settings.xml");	


	@BeforeClass
	public static void before() {
		before( settings);
	}
		
	
	protected String[] getResultsForFirstRun() {
		return new String [] {
				"com.braintribe.test.dependencies.parentLookupTest:A#1.0",				
				"com.braintribe.test.dependencies.parentLookupTest:B#1.0",
				"com.braintribe.test.dependencies.parentLookupTest:C#1.0",		
		};
	}
	
	protected void tweakEnvironment() {
		scope.clear();
		repositoryRegistry.clear();		
	}


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

	private static void launchRepolets(int port) {
		Map<String, RepoType> map  = new HashMap<String, LauncherShell.RepoType>();
		map.put( "archive," + data[0].getAbsolutePath(), RepoType.singleZip);
		launcherShell = new LauncherShell( port);
		launcherShell.launch( map);

	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}
	
	@Test
	public void testUpdate() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.parentLookupTest");
			terminal.setArtifactId( "ParentLookupTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			
			// first test: set with only #1.0 versions 
			String[] expectedNames = getResultsForFirstRun();
			
			Collection<Solution> result = test( "testupdate", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
			
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	
	
		
	
}
