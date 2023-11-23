// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt.testrun;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.braintribe.build.gwt.GwtModuleChecker;
import com.braintribe.build.gwt.ModuleCheckProtocol;
import com.braintribe.build.gwt.ModuleClassDependency;
import com.braintribe.utils.IOTools;

public class ClasspathFileUnsatisfiedDepsTest {
	public static void main(String[] args) throws Exception {
		try {
			File baseFolder = new File(
					"C:\\svn\\artifacts\\com\\braintribe\\model\\processing\\BasicNotifyingGmSession\\1.0");
			File srcFolder = new File(baseFolder, "src");
			File classesFolder = new File(baseFolder, "bin");
			GwtModuleChecker checker = new GwtModuleChecker();
			checker.setStrict(false);
			checker.setArtifactClassFolder(classesFolder);
			checker.setClasspath(getClassPath());
			checker.setSrcPath(srcFolder);
			Set<ModuleCheckProtocol> protocols = checker.check();
			
			for (ModuleCheckProtocol protocol: protocols) {
				System.out.println("problems found for module " + protocol.getModuleName() + ":");
				for (ModuleClassDependency dependency: protocol.getUnsatisfiedDependencies()) {
					System.out.println();
					System.out.println("  unsatisfied dependency to class: " + dependency.getClassName());
					if (dependency.getPathToSource() != null)
						System.out.println("  found source for class but not within any inherited module");
					else
						System.out.println("  no source found for class");
					
					System.out.println("  The class is required by the following module classes:");
					for (String dependingClass: protocol.getClassesDependingClass(dependency.getClassName())) {
						System.out.println("    " + dependingClass);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static List<File> getClassPath() throws Exception {
		List<File> classpath = new ArrayList<File>();
		
		String fName = "C:\\braintribe\\workspace\\TFH2\\.metadata\\.plugins\\com.braintribe.eclipse.plugin.FileClassPathLocalJavaApplicationLauncher\\TestMain.classpath"; 
		String allLinesTogether = IOTools.slurp(new File(fName), "UTF-8");
		String[] lines = allLinesTogether.split("\n");
		
		for (String l: lines) {
			classpath.add(new File(l));
		}
		
		return classpath;
	}
}
