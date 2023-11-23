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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository.purge;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.ArtifactRemover;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.utils.paths.UniversalPath;

/**
 * simple tests to check the 'recursive deleter' that can report if it couldn't delete  
 * all files - actually, returns a list of files it couldn't delete, and NONE if all were deleted.
 * @author pit
 *
 */
public class FolderRemovalTest implements HasCommonFilesystemNode {

	
	protected File repoInstall;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/repository/purge-delete");
		input = pair.first;
		output = pair.second;
		repoInstall = new File( output, "install");
	}	
	protected File initialRepo = new File( input, "install-repo");
	
	@Before
	public void runBefore() {

		// install repo -> install 
		TestUtils.ensure(repoInstall);
		if (initialRepo.exists()) {
			TestUtils.copy(initialRepo, repoInstall);
		}				
	}
	
	@Test 
	public void testFullDelete() {
		
		String groupId = "com.braintribe.devrock.test";
		File deleteTarget = UniversalPath.from(repoInstall).pushDottedPath( groupId).push("a").toFile();
		
		List<File> delete = ArtifactRemover.delete( deleteTarget);
		
		Validator validator = new Validator();
		
		validator.assertTrue("unexpectedly, the delete target still exists:" + deleteTarget.getAbsolutePath(), !deleteTarget.exists());
		
		validator.assertResults();
		
		System.out.println(delete);
	}
	
	@Test 
	public void testSelectiveDelete() {
		
		String groupId = "com.braintribe.devrock.test";
		File deleteTarget = UniversalPath.from(repoInstall).pushDottedPath( groupId).push("b").push("1.0.1-pc").toFile();
		
		List<File> delete = ArtifactRemover.delete( deleteTarget);
		
		Validator validator = new Validator();
		
		validator.assertTrue("unexpectedly, the delete target still exists:" + deleteTarget.getAbsolutePath(), !deleteTarget.exists());
		
		validator.assertResults();
		
		System.out.println(delete);
	}
	
	@Test 
	public void testUnsafeDeleteDetection() {
		
		String groupId = "com.braintribe.devrock.test";
		File deleteTarget = UniversalPath.from(repoInstall).pushDottedPath( groupId).push("a").toFile();
		
		boolean canBeDeleted = ArtifactRemover.canBeSafelyDeleted(deleteTarget);
		
		Validator validator = new Validator();
		
		validator.assertTrue("unexpectedly, safe delete is possible on :" + deleteTarget.getAbsolutePath(), !canBeDeleted);
		
		validator.assertResults();			
	}
	
	@Test 
	public void testSafeDeleteDetection() {
		
		String groupId = "com.braintribe.devrock.test";
		File deleteTarget = UniversalPath.from(repoInstall).pushDottedPath( groupId).push("c").toFile();
		
		boolean canBeDeleted = ArtifactRemover.canBeSafelyDeleted(deleteTarget);
		
		Validator validator = new Validator();
		
		validator.assertTrue("unexpectedly, safe delete is impossible on :" + deleteTarget.getAbsolutePath(), canBeDeleted);
		
		validator.assertResults();			
	}
	
	
}
