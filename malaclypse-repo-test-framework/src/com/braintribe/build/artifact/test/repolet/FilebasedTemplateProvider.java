// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import com.braintribe.utils.IOTools;

public class FilebasedTemplateProvider implements Supplier<String>{

	private String cachedContents;
	private File template;
	
	public FilebasedTemplateProvider( File template) {
		this.template = template;
		
	}

	@Override
	public String get() throws RuntimeException {
		if (cachedContents != null)
			return cachedContents;
		try {
			cachedContents = IOTools.slurp(template, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException( "cannot load file [" + template.getAbsolutePath() + "]", e);
		}
		return cachedContents;
	}
	
	
	
}
