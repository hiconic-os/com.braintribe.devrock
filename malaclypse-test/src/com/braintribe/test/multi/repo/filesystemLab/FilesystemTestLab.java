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
package com.braintribe.test.multi.repo.filesystemLab;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.AbstractWalkLab;
import com.braintribe.test.multi.ClashStyle;
import com.braintribe.test.multi.WalkDenotationTypeExpert;
import com.braintribe.utils.FileTools;



public class FilesystemTestLab extends AbstractWalkLab {
	private static File localRepository = new File ("res/fileSystemRepoLab/contents/repo/local");
	private static File remoteRepository = new File ("res/fileSystemRepoLab/contents/repo/remote");
	private static File baseRepository = new File ("res/fileSystemRepoLab/contents/repo/base");
	private static File settings = new File( "res/fileSystemRepoLab/contents/settings.xml");
	
	@BeforeClass
	public static void before() {				
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		localRepository.mkdirs();
		
		// clean remote repository
		TestUtil.delete(remoteRepository);
		
		// prime remote repository 
		try {
			FileTools.copyDirectory(baseRepository, remoteRepository);
		} catch (IOException e) {
			Assert.fail("cannot copy base directory [" + baseRepository + "] to [" + remoteRepository);
		}
		
		runBefore();
		
		// clean local repository
		TestUtil.delete(localRepository);				
	}
	
	private void walkTest(Solution terminal, String ... expectedNames) {		
		try {
			Collection<Solution> result = test( "testRepositoryRole", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
		} catch (Exception e) {
			Assert.fail("exception [" + e.getMessage() + "] thrown");
		}
	}

	
	private void uploadTest() {		
	}
	
	
	
	@Test
	public void test() {
		try {
			Solution solution = Solution.T.create();
			solution.setGroupId("com.braintribe.test.dependencies.complexImportDependencyTest");
			solution.setArtifactId("ComplexImportDependencyTestTerminal");
			solution.setVersion( VersionProcessor.createFromString( "1.0"));
			
			walkTest( solution, "com.braintribe.test.dependencies.complexImportDependencyTest:A#1.0");
		} catch (VersionProcessingException e) {
			Assert.fail("exception [" + e.getMessage() + "] thrown");
			return;
		}
		// access test
	}

}
