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
