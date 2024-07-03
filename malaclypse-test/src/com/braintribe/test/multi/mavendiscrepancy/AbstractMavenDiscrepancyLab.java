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
package com.braintribe.test.multi.mavendiscrepancy;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

/**
 * MavenDiscrepancyTestTerminal
 * 	A#1.0
 * 		B#1.1
 *  (A#1.1)
 *  	C#1.0
 * 	B#1.0
 * 		A#1.1
 * 		C#1.0
 *  (B#1.1)
 *  	C#1.1
 *  (C#1.0)
 *  (C#1.1)
 *  
 * @author pit
 *
 */
public abstract class AbstractMavenDiscrepancyLab extends AbstractWalkLab {
	private static LauncherShell launcherShell;
	private static File contents = new File( "res/mavenDiscrepancyLab/contents");
	protected static File localRepository = new File (contents, "repo");
	private static final File [] data = new File[] { new File( contents, "archive.zip"),													 
	};

	protected static Map<String, Repolet> launchedRepolets;	
	

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
		launchedRepolets = launcherShell.launch( map);
	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}
	
		
	public void runTest( String [] expectedNames, ClashStyle clashStyle) {
		try {
			Solution terminal = Solution.T.create();			
			terminal.setGroupId( "com.braintribe.test.dependencies.mavenDiscrepancyTest");
			terminal.setArtifactId( "MavenDiscrepancyTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			
			Collection<Solution> result = test( "testupdate", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((clashStyle)), expectedNames,1,0);
			testPresence(result, localRepository);			
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
		
}
