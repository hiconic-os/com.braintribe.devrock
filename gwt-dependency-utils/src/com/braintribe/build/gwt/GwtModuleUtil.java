// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt;

import java.io.File;

public class GwtModuleUtil {
	private File artifactClassesFolder;
	private File artifactSourceFolder;
	
	public void setArtifactClassesFolder(File artifactClassesFolder) {
		this.artifactClassesFolder = artifactClassesFolder;
	}

	public void setArtifactSourceFolder(File artifactSourceFolder) {
		this.artifactSourceFolder = artifactSourceFolder;
	}
	
	public String getClassNameFromClassFile(File classFile) {
		return getModuleOrClassNameFromClassFile(artifactClassesFolder, classFile, ".class");
	}
	
	public String getModuleNameFromModuleFile(File moduleFile) {
		return getModuleOrClassNameFromClassFile(artifactSourceFolder, moduleFile, ".gwt.xml");
	}
	
	public String getModuleOrClassNameFromClassFile(File rootFolder, File file, String suffix) {
		String basePath = rootFolder.getPath();
		String fullPath = file.getPath();
		String relativePath = fullPath.substring(basePath.length() + 1);
		String className = relativePath.replace(File.separatorChar, '.').substring(0, relativePath.length() - suffix.length());
		return className;
	}

}
