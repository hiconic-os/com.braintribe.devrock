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
package com.braintribe.test.multi.redirectionLab;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifact.test.repolet.Repolet;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDomain;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.OptimisticClashResolverDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.DependencyScope;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.AbstractWalkLab;
import com.braintribe.test.multi.WalkDenotationTypeExpert;

public abstract class AbstractRedirectionLab extends AbstractWalkLab {
	private static LauncherShell launcherShell;
	protected static File contents = new File( "res/redirection/contents");
	protected static File localRepository = new File (contents, "repo");
	private static final File [] data = new File[] { 
			new File( contents, "archive.zip"),
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
	
	protected void runTest(String terminalAsString, String [] expectedNames) {
		runTest( terminalAsString, expectedNames, ScopeKind.compile, WalkKind.classpath, true);
	}
	
	
	protected void runTest(String terminalAsString, String [] expectedNames, ScopeKind scopeKind, WalkKind walkKind, boolean skipOptional, DependencyScope ... additionalScopes) {
		try {
			Solution terminal = NameParser.parseCondensedSolutionName(terminalAsString);
			
			ClashResolverDenotationType clashResolverDt = OptimisticClashResolverDenotationType.T.create();
			WalkDenotationType walkDenotationType = WalkDenotationTypeExpert.buildCompileWalkDenotationType(  clashResolverDt, scopeKind, WalkDomain.repository, walkKind, skipOptional, additionalScopes);
			Collection<Solution> result = test( "runSubTreeExclusionTest", terminal, walkDenotationType, expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
		
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	

}
