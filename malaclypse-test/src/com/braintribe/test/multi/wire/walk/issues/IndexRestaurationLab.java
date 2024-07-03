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
package com.braintribe.test.multi.wire.walk.issues;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.wire.AbstractRepoletWalkerWireTest;
import com.braintribe.wire.api.context.WireContext;

public class IndexRestaurationLab extends AbstractRepoletWalkerWireTest {
	private File preparation = new File( testSetup, "repo");	
	private Map<String, RepoType> launcherMap;
	
	{
		launcherMap = new HashMap<>();		
		launcherMap.put( "archive," + new File( testSetup, "archive.zip").getAbsolutePath(), RepoType.singleZip);
						
	}
	
	@Override
	protected File getRoot() {	
		return new File("res/wire/issues/index-restauration");
	}
		
	@Before
	public void before() {		
		// copy old state 
		runBefore(launcherMap);
		TestUtil.copy( preparation, repo);
	}
	
	@After
	public void after() {
		runAfter();
	}
	
	@Test
	public void test() {
		WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( new File( testSetup, "settings.xml"), repo, overridesMap);
		
		RepositoryReflection repositoryReflection = classpathWalkContext.contract().repositoryReflection();
		
		List<String> newGroups = repositoryReflection.correctLocalRepositoryStateOf("braintribe.Base");
		
		System.out.println( newGroups.stream().collect(Collectors.joining(",")));
		
		
	}

}
