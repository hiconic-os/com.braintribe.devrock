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
package com.braintribe.devrock.mc.core.repository.test;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.repolet.Repolet;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.testing.category.KnownIssue;

/**
 * a framework for tests based on the {@link Repolet} feature,
 * provides basic tools to handle them.
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public abstract class AbstractRepoletBasedTest implements HasCommonFilesystemNode {
	protected Launcher launcher;	
	protected Map<String, String> launchedRepolets;

	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("repository");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	
	/**
	 * call in your test's {@code @before} function
	 * @param map - the repolet info to use for launching
	 * @return - the port used 
	 */
	protected void runBefore() {		 		
		
		launchedRepolets = launcher.launch();
		
		protocolLaunch( "launching repolets:");			
	}	
	/**
	 * call in your test's {@code @after} function 
	 */
	protected void runAfter() {
		protocolLaunch("shutting down repolets");	
		launcher.shutdown();		
	}

	/**
	 * @param prefix - print a string with all repo names
	 */
	private void protocolLaunch(String prefix) {
		String v = launchedRepolets.keySet().stream().collect(Collectors.joining(","));				
		System.out.println( prefix + ":" + v);
	}
	
}
