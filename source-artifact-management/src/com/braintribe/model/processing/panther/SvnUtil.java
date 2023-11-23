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
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.braintribe.utils.xml.XmlUtil;


public class SvnUtil {
	public static Document list(String url) throws SvnException {
		//String pattern = "svn list --xml \"{0}\"";
		//String cmd = MessageFormat.format(pattern, url);
		String[] cmd = {"svn", "list", "--xml", url};
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd);
		
		if (svnOutput.getRetVal() == 0) {
			try {
				return XmlUtil.parseXML(svnOutput.getNormalText());
			} catch (Exception e) {
				throw new SvnException("error while parsing result xml", e);
			}
		}
		else {
			throw new SvnException(svnOutput.getErrorText());
		}
	}
	
	public static void del(String url, String message) throws SvnException {
		//String pattern = "svn del \"{0}\" -m \"{1}\"";
		//String cmd = MessageFormat.format(pattern, url, message);
		String[] cmd = {"svn", "del", url, "-m", message};
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd);
		
		if (svnOutput.getRetVal() == 0) {
			return;
		}
		else {
			throw new SvnException(svnOutput.getErrorText());
		}
	}
	
	public static void export(String fromUrl, File toFile) throws SvnException {
		//String pattern = "svn export \"{0}\" \"{1}\"";
		//String cmd = MessageFormat.format(pattern, fromUrl, toFile.toString());
		String[] cmd = {"svn", "export", fromUrl, toFile.toString()};
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd);
		
		if (svnOutput.getRetVal() == 0) {
			return;
		}
		else {
			throw new SvnException(svnOutput.getErrorText());
		}
	}
	
	public static void checkout(String fromUrl, File toFile, boolean recursive) throws SvnException {
		//String pattern = "svn co \"{0}\" \"{1}\" {2}";
		String recursiveOption = recursive? "": "-N";
		//String cmd = MessageFormat.format(pattern, fromUrl, toFile.toString(), recursiveOption);
		String[] cmd = {"svn", "co", fromUrl, toFile.toString(), recursiveOption};
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd);
		
		if (svnOutput.getRetVal() == 0) {
			return;
		}
		else {
			throw new SvnException(svnOutput.getErrorText());
		}
	}
	
	public static ProcessResults checkout(String fromUrl, File toFile, boolean recursive, Consumer<ProcessBuilder> configurer) {
		//String pattern = "svn co \"{0}\" \"{1}\" {2}";
		String recursiveOption = recursive? "": "-N";
		//String cmd = MessageFormat.format(pattern, fromUrl, toFile.toString(), recursiveOption);
		String[] cmd = {"svn", "co", fromUrl, toFile.toString(), recursiveOption};
		
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd, configurer);
		return svnOutput;
	}
	
	public static ProcessResults commit(File toFile, String message, Consumer<ProcessBuilder> configurer) {
		String[] cmd = {"svn", "commit", toFile.toString(), "-m", message};
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd, configurer);
		
		return svnOutput;
	}
	
	public static void commit(File toFile, String message) throws SvnException {
		//String pattern = "svn commit \"{0}\" -m \"{1}\"";
		//String cmd = MessageFormat.format(pattern, toFile, message);
		String[] cmd = {"svn", "commit", toFile.toString(), "-m", message};
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd);
		
		if (svnOutput.getRetVal() == 0) {
			return;
		}
		else {
			throw new SvnException(svnOutput.getErrorText());
		}
	}
	
	/**
	 * adds a file to svn 
	 * @param toFile
	 * @throws SvnException
	 */
	public static void add( File toFile) throws SvnException {
		String[] cmd = {"svn", "add", toFile.toString()};
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd);
		
		if (svnOutput.getRetVal() == 0) {
			return;
		}
		else {
			throw new SvnException(svnOutput.getErrorText());
		}
	}

	
	public static File exportToTemporaryFile(String fromUrl) throws SvnException {
		try {
			File file = File.createTempFile("svn-export", null);
			export(fromUrl, file);
			return file;
		} catch (IOException e) {
			throw new SvnException(e);
		}
	}
	
	public static void update(File file, boolean recursive) throws SvnException {
		//String pattern = "svn up \"{0}\" \"{1}\"";
		String recursiveOption = recursive? "": "-N";
		//String cmd = MessageFormat.format(pattern, recursiveOption, file.toString());
		String[] cmd = {"svn", "up", recursiveOption, file.toString()};
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd);
		
		if (svnOutput.getRetVal() == 0) {
			return;
		}
		else {
			throw new SvnException(svnOutput.getErrorText());
		}
	}
	
	/**
	 * copies everything from fromUrl to toUrl. 
	 * WARNING: make sure toUrl doesn't exist. If it does, you must use branchContentsOnly 
	 * @param fromUrl
	 * @param toUrl
	 * @param message
	 * @throws SvnException
	 */
	public static void branch(String fromUrl, String toUrl, String message) throws SvnException {
		//String cmdPattern = "svn copy \"{0}\" \"{1}\" -m \"{2}\"";
		//String cmd = MessageFormat.format(cmdPattern, fromUrl, toUrl, message);
		String[] cmd = {"svn", "copy", fromUrl, toUrl, "-m", message};
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd);
		
		if (svnOutput.getRetVal() == 0) {
			return;
		}
		else {
			throw new SvnException(svnOutput.getErrorText());
		}
	}
	
	/**
	 * copies everything from fromUrl to toUrl.
	 * WARNING: make sure toUrl does exist. If it doesn't, you must either use branch (anyway,
	 * it's way faster) or mkdir. 
	 * @param fromUrl
	 * @param toUrl
	 * @param message
	 * @throws SvnException
	 */
	public static void branchContentsOnly( String fromUrl, String toUrl, String message) throws SvnException {
		Document doc = list( fromUrl);
		NodeList nodes = doc.getElementsByTagName( "entry");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element entryE = (Element) nodes.item(i);
			Element nameE = XmlUtil.getElementByPath(entryE, "name");
			String urlToUse = fromUrl + "/" + nameE.getTextContent();
			branch( urlToUse, toUrl, message);
		}
	}
	
	public static void ensurePath(String url, String message) throws SvnException {
		//String cmdPattern = "svn mkdir \"{0}\" -m \"{1}\"";
		//String cmd = MessageFormat.format(cmdPattern, url, message);
		String[] cmd = {"svn", "mkdir", url, "-m", message};
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd);
		
		if (svnOutput.getRetVal() == 0) {
			return;
		}
		else {
			throw new SvnException(svnOutput.getErrorText());
		}
	}
	
	public static void mkdir(String url, String message) throws SvnException {
		//String cmdPattern = "svn mkdir \"{0}\" -m \"{1}\"";
		//String cmd = MessageFormat.format(cmdPattern, url, message);
		String[] cmd = {"svn", "mkdir", url, "-m", message};
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd);
		
		if (svnOutput.getRetVal() == 0) {
			return;
		}
		else {
			throw new SvnException(svnOutput.getErrorText());
		}
	}
	
	/**
	 * not very clear what svn returns here.
	 * Currently it returns 1 which leads to SvnInfo throwing an exception.
	 * But that is prone to change - currently it is flux, and depends on svn / client 
	 * versions.. this function should handle most of the cases 
	 * @param url
	 * @return
	 * @throws SvnException
	 */
	public static boolean exists(String url) throws SvnException {
		SvnInfo svnInfo;
		try {
			svnInfo = new SvnInfo(url);
			return svnInfo.isValid();
		} catch (Exception e) { // currently SvnInfo throws an exception when encountering an invalid url.. 
			return false;
		}

	}
	
	public static class SvnInputStream extends FilterInputStream {
		private Process process;
		public SvnInputStream(Process process) {
			super(process.getInputStream());
			this.process = process;
		}
		
		public int getSvnProcessReturnValue() {
			try {
				return this.process.waitFor();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
		
		private static Pattern pattern = Pattern.compile("svn: (E......):");
		
		public String getSvnError() throws IOException {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), getConsoleEncoding()));
				String line = null;
				String code = null;
				while ((line = reader.readLine()) != null) {
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						code = matcher.group(1);
					}
				}
				return code;
			} catch (Exception e) {
				throw new IOException("error while analysing svn output", e);
			}
		}
	}
	
	public static SvnInputStream streamedCat(String url, String revision) throws SvnException {
		try {
			String[] cmd = {"svn", "cat", "-r", revision, url};
			
			Process process = Runtime.getRuntime().exec(cmd, null);
			
			return new SvnInputStream(process);
		} catch (Exception e) {
			throw new SvnException("error while opening process output", e);
		}
	}
	
	public static SvnInputStream streamedList(String url, String revision, boolean recursive) throws SvnException {
		try {
			String[] cmd = recursive? new String[]{"svn", "list", "-R", "-r", revision, url}:new String[]{"svn", "list", "-r", revision, url};
			
			Process process = Runtime.getRuntime().exec(cmd, null);

			return new SvnInputStream(process);
		} catch (Exception e) {
			throw new SvnException("error while opening process output", e);
		}
	}
	
	public static SvnInputStream streamedLog(String url, String fromRevision, String toRevision) throws SvnException {
		try {
			String[] cmd = new String[]{"svn", "log", "-v", "-r", fromRevision+":"+toRevision, url};
			
			Process process = Runtime.getRuntime().exec(cmd, null);
			
			return new SvnInputStream(process);
		} catch (Exception e) {
			throw new SvnException("error while opening process output", e);
		}
	}


	public static String getConsoleEncoding() {
		if (System.getProperty ("os.name").contains("Windows")) 
			return "Cp850";
		else
			return System.getProperty("file.encoding");
	}
}
