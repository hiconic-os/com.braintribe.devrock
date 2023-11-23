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
package com.braintribe.test.multi.shortCutResolvingLab;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;


/**
 *
 * 
 * this is the Terminal artifact for testing MC's behavior when it resolving of ranges. test makes sure that ONLY the top matching
 * version is download. 
 *
 *	short-cut-resolving-test-terminal 
 *	a#1.0.4
 *	
 *	all other a, 1.0.1 - 1.0.3 are not be downloaded, i.e. may not exist in the local repo
 *	
 * 
 * @author pit
 *
 */
public class ShortCutResolvingLab extends AbstractShortCutResolvingLab {
		
	
	protected static File settings = new File( "res/shortCutResolvingLab/contents/settings.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testShortCut() {
		Identification identification = Identification.T.create();
		identification.setGroupId( "com.braintribe.devrock.test.shortcutresolving");
		identification.setArtifactId( "a");
		String version = "1.0.4";
		String[] expectedNames = new String [] {
				identification.getGroupId() + ":" + identification.getArtifactId() + "#" + version 						
		};		
		runTest( "com.braintribe.devrock.test.shortcutresolving:short-cut-resolving-test-terminal#1.0.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
		//
		File artifactDir = RepositoryReflectionHelper.getFilesystemLocation(localRepository, identification);
		File [] artifacts = artifactDir.listFiles( new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory() == false)
					return false;
				return true;
			}
		});
		
		Assert.assertTrue( "unexpected number of directories found : [" + artifacts.length + "]", artifacts.length == 1);
		String name = artifacts[0].getName();
		Assert.assertTrue( "unexpected name of found directory [" + name + "]", name.equalsIgnoreCase( version));
	}
	
	
	

	
}
