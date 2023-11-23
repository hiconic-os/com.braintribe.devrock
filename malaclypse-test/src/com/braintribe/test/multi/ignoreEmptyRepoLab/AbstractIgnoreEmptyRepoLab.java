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
