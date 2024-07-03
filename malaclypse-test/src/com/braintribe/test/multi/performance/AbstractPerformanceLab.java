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
package com.braintribe.test.multi.performance;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.test.repolet.Repolet;
import com.braintribe.logging.JdkLogger;
import com.braintribe.model.artifact.Dependency;
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

public abstract class AbstractPerformanceLab extends AbstractWalkLab {
	protected static File contents = new File( "res/performance/malaclypse");
	protected static File localRepository = new File (contents, "repo");

	protected static Map<String, Repolet> launchedRepolets;	
	
	protected static File determineSettings() {
		String ptr = System.getenv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS");
		File settings;
		if (ptr != null) {
			settings = new File( ptr);
			if (settings.exists())
				return settings;
		}
		String usrHome = System.getProperty( "user.home");
		settings = new File( usrHome + "/.m2/settings.xml");
		if (settings == null || !settings.exists()) {
			String msg = "no settings.xml found, but required for this test";
			Assert.fail(msg);
			throw new IllegalStateException( msg);
		}
		return settings;
	}


	protected static void before( File settings) {
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		runBefore();
		
	
		TestUtil.ensure( localRepository);
		try {
			JdkLogger.readConfiguration( new File( contents, "logger.properties").toURI().toURL());
		} catch (MalformedURLException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
	}

	@AfterClass
	public static void after() {
		runAfter();
	}
	
	
	
	
	protected Collection<Solution> runTest(String terminalAsString, String [] expectedNames, ScopeKind scopeKind, WalkKind walkKind, boolean skipOptional, DependencyScope ... additionalScopes) {
		try {
			Dependency  terminal = NameParser.parseCondensedDependencyName(terminalAsString);
			
			ClashResolverDenotationType clashResolverDt = OptimisticClashResolverDenotationType.T.create();
			WalkDenotationType walkDenotationType = WalkDenotationTypeExpert.buildCompileWalkDenotationType(  clashResolverDt, scopeKind, WalkDomain.repository, walkKind, skipOptional, additionalScopes);
			Collection<Solution> result = test( "performanceRun", terminal, walkDenotationType, expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
			return result;
		
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e + "] thrown");
		}
		return null;
	}
	
	
	protected void run( String terminal) {
		System.out.println("**** 1) running on empty repository ****");
		// running on empty repository 
		long start = System.nanoTime();
		Collection<Solution> result = runTest( terminal, null, ScopeKind.compile, WalkKind.classpath, false);
		long endOfFirstRun = System.nanoTime();
		System.out.println("********");
		
		repositoryRegistry.clear();
		
		// running again
		System.out.println("**** 2) running on filled repository without cache");
 		long startOfSecondRun = System.nanoTime();
		result = runTest( terminal, null, ScopeKind.compile, WalkKind.classpath, false);
 		long endOfSecondRun = System.nanoTime();
		System.out.println("********");
 		
 		// running again
 		System.out.println("**** 3) running on filled  with cache");
 		long startOfThirdRun = System.nanoTime();
 		result = runTest( terminal, null, ScopeKind.compile, WalkKind.classpath, false);
 		long endOfThirdRun = System.nanoTime();
 		
 		System.out.println("results are for [" + result.size() + "] entries");
		result.stream().forEach( s -> System.out.println( NameParser.buildName(s)));
		
 		
 		System.out.println( "Run with empty repository :" + ((endOfFirstRun - start) / 1E6) + " ms");
 		System.out.println( "Run with filled repository, yet no caching :" + ((endOfSecondRun - startOfSecondRun) / 1E6) + " ms");
 		System.out.println( "Run with full cache :" + ((endOfThirdRun - startOfThirdRun) / 1E6) + " ms");
	}

}
