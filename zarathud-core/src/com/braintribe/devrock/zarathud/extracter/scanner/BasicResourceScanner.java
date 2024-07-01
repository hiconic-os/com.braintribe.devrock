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
package com.braintribe.devrock.zarathud.extracter.scanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.braintribe.devrock.zarathud.ZarathudException;
import com.braintribe.devrock.zarathud.extracter.ResourceScanner;

/**
 * a scanner that extracts classes (and gwt module name) from a jar
 * 
 * @author pit
 *
 */
public class BasicResourceScanner implements ResourceScanner {
	
	private static final String classSuffix = ".class";
	private static final String gwtSuffix = ".gwt.xml";
	

	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.scanner.ResourceScanner#scanJar(java.io.File)
	 */
	@Override
	public ScannerResult scanJar(File file) throws IOException {
		ScannerResult scannerResult = new ScannerResult();
		List<String> result = new ArrayList<String>();
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
			Enumeration<JarEntry> enumeration = jarFile.entries();
			
			while (enumeration.hasMoreElements()) {
				JarEntry jarEntry = enumeration.nextElement();
				String name = jarEntry.getName();
				if (name.endsWith(classSuffix)) {
					String moduleName = name.substring(0, name.length() - classSuffix.length()).replace('/', '.');			
					result.add( moduleName);				
				}
			}
		} catch (Exception e) {
			throw new ZarathudException( "cannot extract classes from [" + file.getAbsolutePath() + "]", e);
		}
		finally {
			if (jarFile != null)
				jarFile.close();
		}
		
		scannerResult.setClasses(result);
		return scannerResult;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.scanner.ResourceScanner#scanJarForGwt(java.io.File)
	 */
	@Override
	public ScannerResult scanJarForGwt( File file) throws IOException {
		ScannerResult scannerResult = new ScannerResult();
	
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
			Enumeration<JarEntry> enumeration = jarFile.entries();
			
			while (enumeration.hasMoreElements()) {
				JarEntry jarEntry = enumeration.nextElement();
				String name = jarEntry.getName();
				if (name.endsWith( gwtSuffix)) {
					String moduleName = name.substring(0, name.length() - gwtSuffix.length()).replace('/', '.');			
					scannerResult.setModuleName(moduleName);				
				}
			}
		} catch (Exception e) {
			throw new ZarathudException( "cannot extract the gwt module name from [" + file.getAbsolutePath() + "]", e);
		}
		finally {
			if (jarFile != null)
				jarFile.close();
		}
		
		return scannerResult;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.scanner.ResourceScanner#scanFolder(java.io.File)
	 */
	@Override
	public ScannerResult scanFolder(File folder) throws IOException {
		return scanFolder(folder, new Stack<String>());
	}
	
	/**
	 * actually scan a folder recursively for classes 
	 * @param folder the {@link File} representing the folder to scan 
	 * @param packageLevels - a {@link Stack} containing the current (sub-) package name
	 * @return - a {@link ScannerResult} with the combined classes
	 * @throws IOException
	 */
	protected ScannerResult scanFolder(File folder, Stack<String> packageLevels) throws IOException {
		ScannerResult scannerResult = new ScannerResult();
	
		File [] files = folder.listFiles();
		// check if any files are found
		if (
				(files == null) ||
				(files.length == 0)
			) {
			return scannerResult;
		}
		
		// iterate over the found files 
		for (File file: files) {
			String fileName = file.getName();
			// current and parent directory 
			if (Arrays.asList("..", ".").contains(fileName))
				continue;
			
			// directory -> go deeper, otherwise add the file 
			if (file.isDirectory())
				try {
					packageLevels.push(file.getName());
					scannerResult.merge( scanFolder(file, packageLevels));
				}
				finally {
					packageLevels.pop();
				}
			else if (fileName.endsWith(classSuffix)){				
				StringBuilder moduleNameBuilder = new StringBuilder();
				
				for (String packageLevel: packageLevels) {
					moduleNameBuilder.append(packageLevel);
					moduleNameBuilder.append('.');
				}
				String simpleModuleName = fileName.substring(0, fileName.length() - classSuffix.length());
				moduleNameBuilder.append(simpleModuleName);
				String qualifiedModuleName = moduleNameBuilder.toString();
				scannerResult.getClasses().add( qualifiedModuleName);	
			}
		}

		return scannerResult;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.scanner.ResourceScanner#scanFolderForGwt(java.io.File)
	 */
	@Override
	public ScannerResult scanFolderForGwt( File folder) {
		return scanFolderForGwt(folder, new Stack<String>());
	}
	
	/**
	 * scans the folder for the gwt module name - only one is expected, if multiple exist, the LAST found is it
	 * @param folder - the {@link File} pointing to the folder
	 * @param packageLevels - a {@link Stack} of (sub-) package names
	 * @return - a {@link ScannerResult} with only the last found module name
	 */
	protected ScannerResult scanFolderForGwt( File folder, Stack<String> packageLevels) {
		ScannerResult scannerResult = new ScannerResult();
		
	
		File [] files = folder.listFiles();
		if (
				(files == null) ||
				(files.length == 0)
			)
			return scannerResult;
		for (File file: files) {
			String fileName = file.getName();
			if (Arrays.asList("..", ".").contains(fileName))
				continue;
			
			if (file.isDirectory()) {
				try {
					packageLevels.push(file.getName());
					scannerResult.merge( scanFolderForGwt(file, packageLevels));
				}
				finally {
					packageLevels.pop();
				}
			} else if (fileName.endsWith( gwtSuffix)) {
				StringBuilder moduleNameBuilder = new StringBuilder();
				
				for (String packageLevel: packageLevels) {
					moduleNameBuilder.append(packageLevel);
					moduleNameBuilder.append('.');
				}
				String moduleName = fileName.substring(0, fileName.length() - gwtSuffix.length());
				scannerResult.setModuleName(moduleNameBuilder.toString() + moduleName);
			}
		}
		return scannerResult;
	}
	
	
	public static void main( String [] args) {
		ResourceScanner scanner = new BasicResourceScanner();
		ScannerResult scannerResult = new ScannerResult();

	
		for (String arg : args) {
			File file = new File( arg);
			
			try {
				if (file.isDirectory()) {
					scannerResult.merge( scanner.scanFolderForGwt(file));
				} else {
					scannerResult.merge(scanner.scanJarForGwt(file));
				}
			} catch (IOException e) {
				System.err.println("cannot scan file [" + arg + "] as " + e);
			}
			System.out.println("Module: " + scannerResult.getModuleName());
			for (String url : scannerResult.getClasses()) {
				System.out.println("Class : " + url);
			}
		}
		
	}
}
