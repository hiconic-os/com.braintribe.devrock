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
package com.braintribe.utils.classloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class FileClassPathClassLoader extends URLClassLoader {
	public static final String CLASSPATH_FILE_PROPERTY = "com.braintribe.classpath.file";
	
	protected static URL[] readClasspathFile() throws FileClassPathClassLoaderException {
		String classpathFile = System.getProperty(CLASSPATH_FILE_PROPERTY);
		
		if (classpathFile == null || classpathFile.length() == 0)
			throw new FileClassPathClassLoaderException("you must configure the system property " + CLASSPATH_FILE_PROPERTY + " with an existing text file containing one classpath element per line");
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(classpathFile), "UTF-8"));
			
			List<URL> urls = new ArrayList<URL>();
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				File file = new File(line);
				URL url = file.toURI().toURL();
				urls.add(url);
			}
			
			return (URL[]) urls.toArray(new URL[urls.size()]);
		} catch (Exception e) {
			throw new FileClassPathClassLoaderException("error while trying to parse the classpath file " + classpathFile, e);
		}
	}
	
	public FileClassPathClassLoader(ClassLoader parent) throws FileClassPathClassLoaderException {
		super(readClasspathFile(), parent);
	}
}
