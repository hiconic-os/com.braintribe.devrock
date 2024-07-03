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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProcessExecution {
	private static class ProcessStreamReader extends Thread {
		private InputStream in;
		private StringBuilder buffer = new StringBuilder();
		
		public ProcessStreamReader(InputStream in) {
			this.in = in;
		}
		
		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, getConsoleEncoding()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (buffer.length() > 0) buffer.append('\n');
					buffer.append(line);
				}
			}
			catch (InterruptedIOException e) {
			}
			catch (IOException e) {
				
			}
		}
		
		public String getStreamResults() {
			return buffer.toString();
		}
		
		public void cancel() {
			if (isAlive()) {
				interrupt();
				try {
					join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static ProcessResults runCommand(String[] cmd) {
		return runCommand(cmd, null);
	}
	
	public static ProcessResults runCommand(String[] cmd, Consumer<ProcessBuilder> configurer) {
		try {
			ProcessBuilder builder = new ProcessBuilder();
			
			if (configurer != null)
				configurer.accept(builder);
			
			builder.command(cmd);
			
			Process process = builder.start();

			ProcessStreamReader errorReader = new ProcessStreamReader(process.getErrorStream());
			ProcessStreamReader inputReader = new ProcessStreamReader(process.getInputStream());
			
			errorReader.start();
			inputReader.start();
			
			process.waitFor();
			int retVal = process.exitValue();

			inputReader.cancel();
			errorReader.cancel();

			return new ProcessResults(retVal, inputReader.getStreamResults(), errorReader.getStreamResults());
		}
		catch (Exception e) {
			throw new ProcessExecutionException("error while executing " + Arrays.asList(cmd).stream().collect(Collectors.joining(" ")), e);
		}
	}
	
	public static String getConsoleEncoding() {
		return "Cp850";
	}


}
