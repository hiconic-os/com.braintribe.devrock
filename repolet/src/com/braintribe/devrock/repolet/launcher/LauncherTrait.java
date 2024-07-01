// ============================================================================
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
