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
package com.braintribe.test.multi.wire;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifact.test.repolet.Repolet;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.util.network.NetworkTools;

public abstract class AbstractRepoletWalkerWireTest extends AbstractWalkerWireTest {

	//protected File res = new File("res/wire/issues/optionals");
	protected File testSetup = new File( getRoot(), "setup");
	protected File repo = new File( getRoot(), "repo");
	protected LauncherShell launcherShell;	
	private Map<String, Repolet> launchedRepolets;
	protected int port;
	
	protected Map<String,String> overridesMap = new HashMap<>();
	
	protected abstract File getRoot();

	protected int runBefore(Map<String, RepoType> map) {
		
		// 
		TestUtil.ensure( repo);		
	
		port = NetworkTools.getUnusedPortInRange(8080, 8100);
		
		overridesMap = new HashMap<>();
		overridesMap.put("port", Integer.toString(port));
		overridesMap.put( "M2_REPO", repo.getAbsolutePath());
		
	
	
		launcherShell = new LauncherShell( port);
		launchedRepolets = launcherShell.launch( map);
		
		protocolLaunch( "launching repolets:");
		
		return port;
	}
	
	protected void runAfter() {
		protocolLaunch("shutting down repolets");	
		launcherShell.shutdown();		
	}

	private void protocolLaunch(String prefix) {
		StringBuilder builder = new StringBuilder();
		launchedRepolets.keySet().stream().forEach( n -> {
			if (builder.length() > 0) {
				builder.append( ",");
			}
			builder.append( n);
		});
		System.out.println( prefix + ":" + builder.toString());
	}

	
}
