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
