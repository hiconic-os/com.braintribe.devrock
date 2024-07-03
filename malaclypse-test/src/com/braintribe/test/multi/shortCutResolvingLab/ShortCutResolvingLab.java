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
