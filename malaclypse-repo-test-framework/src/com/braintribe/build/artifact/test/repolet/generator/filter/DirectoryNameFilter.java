// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet.generator.filter;

import java.io.File;
import java.util.function.Predicate;

public class DirectoryNameFilter implements Predicate<File> {
	
	private String path;
	private int pathLen;
	
	public DirectoryNameFilter(File source) {
		path = source.getAbsolutePath().replace( '\\', '/');
		pathLen = path.length();
	}
	
	 @Override
	public boolean test(File file) {
		 if (file.isDirectory()) {
			String name = file.getAbsolutePath().replace('\\', '/');
			if (name.length() < pathLen) {
				if (path.contains(name))
					return true;
			}
			else {
				if (name.startsWith(path))
					return true;
			}						
			return false;		
		 }		
		return true;		 						
	 }
}
