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
package com.braintribe.test.multi.resolver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Assert;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifact.test.repolet.Repolet;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.AbstractWalkLab;

public abstract class AbstractResolverLab extends AbstractWalkLab {
	private static LauncherShell launcherShell;
	protected static File contents = new File( "res/resolverFeatures/contents");
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

	private static void launchRepolets( int port) {
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
	
	
	
	
	public void runTest(String terminalAsString, String [] expectedNames, boolean topOnly) {
		try {
			Dependency terminal = NameParser.parseCondensedDependencyName(terminalAsString);
			DependencyResolver resolver = resolverFactory.get();
			String walkScopeId = UUID.randomUUID().toString();
			
			Set<Solution> result;
			if (topOnly) {
				result = resolver.resolveTopDependency(walkScopeId, terminal);
			}
			else {
				result = resolver.resolveMatchingDependency(walkScopeId, terminal);
			}
			
			testResult(result, expectedNames);
		
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	

	protected void testResult(Collection<Solution> solutions, String [] expectedNames) {
		if (expectedNames != null ) {
			if (expectedNames.length == 0) {
				Assert.assertTrue("solutions returned", solutions == null || solutions.size() == 0);
			}
			else {
				Assert.assertTrue("No solutions returned", solutions != null && solutions.size() > 0);
			}					
		}
		List<Solution> sorted = new ArrayList<Solution>( solutions);
		
	
		Collections.sort( sorted, new Comparator<Solution>() {

			@Override
			public int compare(Solution arg0, Solution arg1) {
				return arg0.getArtifactId().compareTo( arg1.getArtifactId());					
			}
			
		});
		
		List<String> names = sorted.stream().map( s -> { return NameParser.buildName( s);}).collect( Collectors.toList());
		List<String> foundNames = new ArrayList<>();
		List<String> notFoundNames = new ArrayList<>();
		
		for (String expected : expectedNames) {
			if (names.contains(expected))  {
				foundNames.add( expected);
				names.remove(expected);
			} else
				notFoundNames.add( expected);
		}
	
		Assert.assertTrue( "missing [" + collectionToString( notFoundNames), notFoundNames.isEmpty());
		Assert.assertTrue( "unexpected [" + collectionToString( names), names.isEmpty());
	}
	
	private String collectionToString( Collection<String> collection) {
		return collection.stream().collect( Collectors.joining(","));
	}
}
