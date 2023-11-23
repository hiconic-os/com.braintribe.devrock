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
package com.braintribe.model.processing.panther;

import java.io.File;

public class ProcessTest {
	public static void main(String[] args) {
		File file = new File("C:\\Daten\\braintribe\\svn\\artifacts\\com\\braintribe\\utils\\IoUtils");
		//String cmdPattern = "svn --xml info \"{0}\"";
		//String cmd = MessageFormat.format(cmdPattern, file);
		String[] cmd = {"svn", "--xml", "info", file.toString()};
		
		ProcessResults results = ProcessExecution.runCommand(cmd);
		System.out.println("---- retval ----");
		System.out.println(results.getRetVal());
		System.out.println("---- normal results ----");
		System.out.println(results.getNormalText());
		System.out.println("---- error results ----");
		System.out.println(results.getErrorText());
	}
}
