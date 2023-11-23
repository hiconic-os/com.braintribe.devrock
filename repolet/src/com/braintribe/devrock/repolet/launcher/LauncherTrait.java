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
package com.braintribe.devrock.repolet.launcher;

import java.util.Map;

/**
 * an interface to introduce Repolet launching features to a class (or JUnit test)
 * see the LauncherTest in com.braintribe.devrock:repolet-test for details
 * 
 * @author pit
 *
 */
public interface LauncherTrait {	
	/**
	 * call in your test's {@code @before} function
	 * @param map - the repolet info to use for launching
	 * @return - the port used 
	 */
	default public Map<String, String> runBefore( Launcher launcher) {		 		
		
		Map<String, String> launchedRepolets = launcher.launch();		
		protocolLaunch( "launching repolets:", launchedRepolets);			
		return launchedRepolets;
	}	
	/**
	 * call in your test's {@code @after} function 
	 */
	default public void runAfter(Launcher launcher) {
		protocolLaunch("shutting down repolets", launcher.getLaunchedRepolets());	
		launcher.shutdown( );		
	}

	/**
	 * @param prefix - print a string with all repo names
	 */
	default public void protocolLaunch(String prefix, Map<String, String> launchedRepolets) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : launchedRepolets.entrySet()) {
			if (sb.length() > 0) {
				sb.append(";");
			}
			sb.append( entry.getKey() + "->" + entry.getValue());
		}
		
		log( prefix + ":" + sb.toString());
	}
	
	
	/**
	 * overload this message to get launch/shutdown messages 
	 * @param message - the message of the launcher protocol
	 */
	default void log(String message) {}
}
