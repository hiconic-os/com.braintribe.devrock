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
package com.braintribe.test.multi.walklab;

import java.io.File;
import java.util.Collection;

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

public abstract class AbstractMultiRepoWalkLab extends AbstractWalkLab {	
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
	

	/**
	 * testing clash resolving
	 */
	@Test
	public void subtreeExclusionTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.subtreeexclusiontest");
			terminal.setArtifactId( "A");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));			
			String[] expectedNames = new String [] {
					"com.braintribe.test.dependencies.subtreeexclusiontest:B#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:C#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:D#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:E#1.1",
					"com.braintribe.test.dependencies.subtreeexclusiontest:F#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:G#1.1",
					"com.braintribe.test.dependencies.subtreeexclusiontest:H#1.0",		
					"com.braintribe.test.dependencies.subtreeexclusiontest:T#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:U#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:V#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:X#1.1",
			};
			
			Collection<Solution> result = test( "subTreeExclusion", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	
	@Test
	public void simpleTestOnB() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.subtreeexclusiontest");
			terminal.setArtifactId( "B");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));			
			String[] expectedNames = new String [] {
					"com.braintribe.test.dependencies.subtreeexclusiontest:X#1.0",				
					"com.braintribe.test.dependencies.subtreeexclusiontest:D#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:E#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:F#1.1",
					"com.braintribe.test.dependencies.subtreeexclusiontest:G#1.1",
					"com.braintribe.test.dependencies.subtreeexclusiontest:H#1.0",		
					"com.braintribe.test.dependencies.subtreeexclusiontest:K#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:L#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:M#1.0",					
			};
			
			Collection<Solution> result = test( "simpleTestOnB", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	@Test
	public void simpleTestOnC() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.subtreeexclusiontest");
			terminal.setArtifactId( "C");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));			
			String[] expectedNames = new String [] {
					"com.braintribe.test.dependencies.subtreeexclusiontest:X#1.1",				
					"com.braintribe.test.dependencies.subtreeexclusiontest:D#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:E#1.1",
					"com.braintribe.test.dependencies.subtreeexclusiontest:F#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:H#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:G#1.1",						
					"com.braintribe.test.dependencies.subtreeexclusiontest:T#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:U#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:V#1.0",
					
			};
			
			Collection<Solution> result = test( "simpleTestOnB", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e + "] thrown");
		}
	}

	
	/**
	 * testing clash resolving and dependency merging 
	 */
	@Test
	public void subtreeExclusionMergingTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.subtreeexclusiontest");
			terminal.setArtifactId( "A");
			terminal.setVersion( VersionProcessor.createFromString( "1.1"));			
			String[] expectedNames = new String [] {
					"com.braintribe.test.dependencies.subtreeexclusiontest:B#1.1",
					"com.braintribe.test.dependencies.subtreeexclusiontest:C#1.1",
					"com.braintribe.test.dependencies.subtreeexclusiontest:D#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:E#1.1",
					"com.braintribe.test.dependencies.subtreeexclusiontest:F#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:G#1.1",
					"com.braintribe.test.dependencies.subtreeexclusiontest:H#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:N#1.5",
					"com.braintribe.test.dependencies.subtreeexclusiontest:O#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:Q#1.9",
					"com.braintribe.test.dependencies.subtreeexclusiontest:R#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:T#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:U#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:V#1.0",
					"com.braintribe.test.dependencies.subtreeexclusiontest:X#1.1",
			};
			
			Collection<Solution> result = test( "subtreeMerging", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e + "] thrown");
		}
	}

}
