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
package com.braintribe.test.multi.biasLab;

import java.io.File;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Assert;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDomain;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.OptimisticClashResolverDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.DependencyScope;
import com.braintribe.test.multi.AbstractWalkLab;
import com.braintribe.test.multi.WalkDenotationTypeExpert;

public abstract class AbstractComplexBiasLab extends AbstractWalkLab {
	private static LauncherShell launcherShell;
	private static File contents = new File( "res/biasLab2/contents");
	protected static File base = new File( contents, "base");	
	protected static File localRepository = new File (contents, "repo");	

	
	

	private static final File [] data = new File[] { new File( contents, "archiveA.zip"), 
													 new File( contents, "archiveB.zip"),
													 new File( contents, "archiveC.zip"),
													 new File( contents, "archiveD.zip"),
	};

	
	protected static void before(File settings) {
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		localRepository.mkdirs();
		int port = runBefore();
		
			
		// fire them up 
		launchRepolets( port);
	}
	
	private static void launchRepolets( int port) {
		String [] args = new String[1];
		args[0] = 	"archiveA," + data[0].getAbsolutePath() + 
					";archiveB," + data[1].getAbsolutePath() + 
					";archiveC," + data[2].getAbsolutePath() +
					";archiveD," + data[3].getAbsolutePath();				
		launcherShell = new LauncherShell( port);
		launcherShell.launch(args,  RepoType.singleZip);
	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}
	
	
	
	public void runTest(String terminalAsString, String [] expectedNames, ScopeKind scopeKind, WalkKind walkKind, boolean skipOptional, DependencyScope ... additionalScopes) {
		try {
			Solution terminal = NameParser.parseCondensedSolutionName(terminalAsString);
			
			ClashResolverDenotationType clashResolverDt = OptimisticClashResolverDenotationType.T.create();
			WalkDenotationType walkDenotationType = WalkDenotationTypeExpert.buildCompileWalkDenotationType(  clashResolverDt, scopeKind, WalkDomain.repository, walkKind, skipOptional, additionalScopes);
			Collection<Solution> result = test( "runTypeTest", terminal, walkDenotationType, expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
		
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	

}
