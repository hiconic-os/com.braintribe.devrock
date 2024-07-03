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
package com.braintribe.test.multi.ignoreEmptyRepoLab;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.AbstractWalkLab;
import com.braintribe.test.multi.ClashStyle;
import com.braintribe.test.multi.WalkDenotationTypeExpert;

public abstract class AbstractIgnoreEmptyRepoLab extends AbstractWalkLab {	
	protected static File localRepository;
	protected static LauncherShell launcherShell;
	
	//protected static File contents; 
	protected static File [] data; 
			
	public static int before(File settings, File localRepository) {
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		localRepository.mkdirs();
		int port = runBefore();
		
		// clean local repository
		TestUtil.delete(localRepository);
		return port;
	}
	
	public static void after() {
		runAfter();
	}
	
	
	@Test
	public void test() {
		testRun();
		System.out.println("--- second run ---");
		testRun();
	}

	/**
	 * testing clash resolving
	 */	
	public void testRun() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.devrock.test.bias");
			terminal.setArtifactId( "bias-terminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0.1"));			
			String[] expectedNames = new String [] {
					"com.braintribe.devrock.test.bias:a#1.0.3",
					"com.braintribe.devrock.test.bias:b#1.0.1",			
			};
			
			Collection<Solution> result = test( "JustToGetSomeFilesInTheLocalRepo", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
			
			String [] namesFromBase = new String [] {	"com.braintribe.devrock.test.bias:parent#1.0.1", 
														"com.braintribe.devrock.test.bias:bias-terminal#1.0.1"
			};
			String [] namesFromAddon = new String [] {	"com.braintribe.devrock.test.bias:a#1.0.3",
														"com.braintribe.devrock.test.bias:b#1.0.1",			
			};
			
			Map<String, List<String>> map = new HashMap<>();
			map.put( "braintribe.Base", Arrays.asList( namesFromBase));
			map.put( "braintribe.AddOn", Arrays.asList( namesFromAddon));
			
			String repository = localRepositoryLocationProvider.getLocalRepository(null);
			testCollateralFiles( new File(repository), map);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e + "] thrown");
		}
	}

	protected abstract void testCollateralFiles(File localRepository, Map<String, List<String>> names); 
		
}
