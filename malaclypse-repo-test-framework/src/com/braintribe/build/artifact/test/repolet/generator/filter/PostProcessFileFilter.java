// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet.generator.filter;

import java.io.File;
import java.util.function.Predicate;

public class PostProcessFileFilter implements Predicate<File> {
	private String name;
	private boolean exclude;
	
	public PostProcessFileFilter(String name, boolean exclude) {
		this.name = name;
		this.exclude = exclude;		
	}

	@Override
	public boolean test(File file) {
		String fileName = file.getName();
		// keep poms.. 
		if (fileName.matches( ".*.pom.*")){
			return true;
		}
		if (fileName.matches( "maven-metadata.xml.*")){
			return true;
		}
		boolean match = fileName.matches( ".*" + name + ".*");
		if (exclude)
			return !match;
		return match;
	}

}
