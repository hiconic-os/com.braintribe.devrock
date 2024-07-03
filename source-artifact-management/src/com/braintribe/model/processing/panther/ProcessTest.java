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
