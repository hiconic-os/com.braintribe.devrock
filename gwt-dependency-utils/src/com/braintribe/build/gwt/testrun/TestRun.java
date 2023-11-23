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
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.braintribe.build.gwt.GwtModuleChecker;
import com.braintribe.build.gwt.ModuleCheckProtocol;
import com.braintribe.build.gwt.ModuleClassDependency;

public class TestRun {
	public static void main(String[] args) {
		try {
			
			File baseFolder = new File("C:\\svn\\artifacts\\com\\braintribe\\utils\\FormatUtil\\1.0");
			File srcFolder = new File(baseFolder, "src");
			File classesFolder = new File(baseFolder, "classes");
			GwtModuleChecker checker = new GwtModuleChecker();
			checker.setStrict(false);
			checker.setArtifactClassFolder(classesFolder);
			checker.setClasspath(getClassPath(baseFolder));
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
