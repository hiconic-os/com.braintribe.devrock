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
