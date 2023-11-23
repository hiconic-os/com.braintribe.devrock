// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt.testrun;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.braintribe.build.gwt.GwtDependencyCollector;
import com.braintribe.build.gwt.GwtModule;
import com.braintribe.build.gwt.GwtModuleChecker;
import com.braintribe.build.gwt.ModuleScanner;
import com.braintribe.utils.IOTools;

public class ClasspathFileDependencyCollectorTest {
	public static void main(String[] args) {
		try {
			File baseFolder = new File("C:\\svn\\artifacts\\com\\braintribe\\gwt\\GwtWebGmRpcClient\\1.0");
			GwtDependencyCollector collector = new GwtDependencyCollector();

			File sourceFolder = new File(baseFolder, "src");
			File classesFolder = new File(baseFolder, "bin");
			collector.setArtifactClassesFolder(classesFolder);
			collector.setArtifactSourceFolder(sourceFolder);
			collector.setClasspath(getClassPath());
			collector.setModuleName("com.braintribe.gwt.gmrpc.web.GmWebRpcClient");
			collector.scanForModules();
			for (GwtModule module: collector.getRequiredModules())
				System.out.println(module);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<File> getClassPath() throws Exception {
		List<File> classpath = new ArrayList<File>();

		String fName = "C:\\braintribe\\workspace\\TFH2\\.metadata\\.plugins\\com.braintribe.eclipse.plugin.FileClassPathLocalJavaApplicationLauncher\\CPGEN_1.classpath";
		String allLinesTogether = IOTools.slurp(new File(fName), "UTF-8");
		String[] lines = allLinesTogether.split("\n");

		for (String l: lines) {
			l = l.trim();
			classpath.add(new File(l));
		}

		return classpath;
	}

}
