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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.lockpurge;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * 
 * base class for all tests for 'lock-file purger'  
 * @author pit
 *
 */
public abstract class AbstractTransitiveRepositoryPurgerTest implements LauncherTrait, HasCommonFilesystemNode {	
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/lockpurge");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	/**
	 * deriving classes overload this 
	 * @return - the {@link File} that points to the initial local repository contents
	 */
	protected File initial() { 
		return new File( input, "initial-empty");
	}
				
	/**
	 * @return - the {@link File} pointing to the maven settings.xml
	 */
	protected abstract File settings();
	
	// not really used, but  still ... 
	private Launcher launcher; 	
	{
		launcher = Launcher.build()
				.repolet()
				.name("archive")
					.descriptiveContent()
						.descriptiveContent( RepoletContent.T.create())
					.close()
				.close()
				.done();
	}
	
	
	@Before
	public void runBefore() {
		TestUtils.ensure(repo); 			
		launcher.launch();
		// copy initial data (mimic local repository)
		File initialRepoContent = initial();
		if (initialRepoContent.exists()) {
			TestUtils.copy( initialRepoContent, repo);
		}		
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	
	
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("M2_REPO", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings().getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}
				
}

