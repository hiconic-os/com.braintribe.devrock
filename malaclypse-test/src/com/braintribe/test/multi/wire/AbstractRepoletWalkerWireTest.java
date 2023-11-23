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
