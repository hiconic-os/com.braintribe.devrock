// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt.testrun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.build.gwt.GwtDependencyCollector;
import com.braintribe.build.gwt.GwtModule;

public class DependencyCollectorTest {
	public static void main(String[] args) {
		try {
			File baseFolder = new File("C:\\svn\\artifacts\\com\\braintribe\\utils\\FormatUtil\\1.0");
			GwtDependencyCollector collector = new GwtDependencyCollector();

			File sourceFolder = new File(baseFolder, "src");
			File classesFolder = new File(baseFolder, "bin");
			collector.setArtifactClassesFolder(classesFolder);
			collector.setArtifactSourceFolder(sourceFolder);
			collector.setClasspath(getClassPath(baseFolder));
			collector.setModuleName("com.braintribe.utils.format.FormatTool");
			collector.scanForModules();
			for (GwtModule module: collector.getRequiredModules())
				System.out.println(module);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static List<File> getClassPath(File baseFolder) throws Exception {
		String fileName = "C:\\Java\\workspaces\\bt-prototype-workspace\\.metadata\\.plugins\\com.braintribe.eclipse.plugin.FileClassPathLocalJavaApplicationLauncher\\Pseudo (1).classpath";
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "ISO-8859-1"));
		String line = null;
		List<File> classpath = new ArrayList<File>();
		
		while ((line = reader.readLine()) != null) {
			classpath.add(new File(line));
		}
		
		reader.close();
		
		return classpath;
	}


}
