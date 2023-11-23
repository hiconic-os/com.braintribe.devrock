// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GwtDependencyCollector extends ModuleScanner {
	private static final String COM_GOOGLE_GWT_EMUL_EMULATION = "com.google.gwt.emul.Emulation";
	private static final String COM_GOOGLE_GWT_CORE_CORE = "com.google.gwt.core.Core";
	private static final String COM_GOOGLE_GWT_EMUL_EMULATION_WITH_USER_AGENT = "com.google.gwt.emul.EmulationWithUserAgent";
	private Set<String> scannedTopLevelClasses = new HashSet<String>();
	private URLClassLoader urlClassLoader;
	private File artifactSourceFolder;
	private File artifactClassesFolder;
	private String moduleName;
	private Set<GwtModule> requiredModules = new HashSet<GwtModule>();	
	
	public GwtDependencyCollector()
			throws Exception {
		super();
	}
		
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public void setArtifactSourceFolder(File artifactSourceFolder) {
		this.artifactSourceFolder = artifactSourceFolder;
	}

	public void setArtifactClassesFolder(File artifactClassesFolder) {
		this.artifactClassesFolder = artifactClassesFolder;
	}
	
	public URLClassLoader getUrlClassLoader() throws MalformedURLException {
		if (urlClassLoader == null) {
			List<File> classpath = getClasspath();
			URL urls[] = new URL[classpath.size() + 1];
			int i = 0;
			urls[i++] = artifactSourceFolder.toURI().toURL();
			for (File file: classpath) {
				urls[i++] = file.toURI().toURL();
			}
			
			urlClassLoader = new URLClassLoader(urls);
			
		}
		return urlClassLoader;
	}
	
	@Override
	public void scanForModules() throws Exception {
		super.scanForModules();
		scanFolder(artifactSourceFolder);
		
		List<File> classFiles = getModuleClassFiles();
		Set<String> allClasses = new HashSet<String>();
		
		for (File classFile: classFiles) {
			Set<String> dependencies = AsmClassDepScanner.getClassDependencies(classFile);
			allClasses.addAll(dependencies);
		}

		for (String className: allClasses) {
			scanForClassModule(className);
		}
		
		// workaround to get replace EmulWithUserAgent/Emulation with simple core reference (the former may cause error while building - user.agent not found )
		// see docu below
		fixGoogleEmulationModules();
	}

	private void fixGoogleEmulationModules() {
		List<GwtModule> badModules = new ArrayList<GwtModule>();
		for (GwtModule module: requiredModules) {
			if (
					module.getModuleName().equals(COM_GOOGLE_GWT_EMUL_EMULATION_WITH_USER_AGENT) ||
					module.getModuleName().equals(COM_GOOGLE_GWT_EMUL_EMULATION)
				) {
				badModules.add(module);
				break;
			}
		}
		
		if (badModules.size() > 0) {
			// as google states about the com.google.gwt.emul.Emulation module, in the gwt-user-2.4.0.jar
			// [
			// A JavaScript-based emulation of the Java Runtime library.
			// Do not inherit this module directly; inherit com.google.gwt.core.Core.
			//GwtModule goodModule = aquireModule(COM_GOOGLE_GWT_EMUL_EMULATION);
			// ]
			GwtModule goodModule = aquireModule(COM_GOOGLE_GWT_CORE_CORE);
			
			requiredModules.remove(badModules);
			requiredModules.add(goodModule);
		}
	}

	public Set<GwtModule> getRequiredModules() {
		return requiredModules;
	}
	
	protected File getModuleRootFolder() throws Exception {
		File srcMmoduleFile = new File(artifactSourceFolder, moduleName.replace('.', '/') + ".gwt.xml");
		File classesModuleFile = new File(artifactClassesFolder, moduleName.replace('.', '/') + ".gwt.xml");
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(srcMmoduleFile);
		NodeList sourceElements = document.getElementsByTagName("source");
		
		String path = "client";
		
		for (int i = 0; i < sourceElements.getLength(); i++) {
			Element sourceElement = (Element)sourceElements.item(i);
			path = sourceElement.getAttribute("path");
			break;
		}
		
		File moduleRootFolder = new File(classesModuleFile.getParentFile(), path);
		
		return moduleRootFolder;
	}

	
	protected List<File> getModuleClassFiles() throws Exception {
		File moduleRootFolder = getModuleRootFolder();
		
		List<File> classFiles = new LinkedList<File>();
		collectAllClassFiles(moduleRootFolder, classFiles);
		return classFiles;
	}
	
	protected void collectAllClassFiles(File folder, List<File> files) {
		for (File file: folder.listFiles()) {
			if (file.getName().toLowerCase().endsWith(".class"))
				files.add(file);
			if (file.isDirectory() && !(file.getName().equals(".") || file.getName().equals(".."))) {
				collectAllClassFiles(file, files);
			}
		}
	}

	protected void scanForClassModule(String className) {
		int index = className.lastIndexOf('.');
		String packageName = className.substring(0, index);
		String classDeclarationName = className.substring(index + 1);
		
		index = classDeclarationName.indexOf('$');
		
		String topLevelSimpleClassName = index == -1? classDeclarationName: classDeclarationName.substring(0, index);
		String topLevelClassName = packageName + '.' + topLevelSimpleClassName;
		
		ensureScanned(topLevelClassName);
	}
	
	private void ensureScanned(String topLevelClassName) {
		if (scannedTopLevelClasses.contains(topLevelClassName))
			return;

		try {
			// first check non emulated
			String resourcePath = topLevelClassName.replace('.', '/') + ".java";
			if (!scanTopLevelClass(topLevelClassName, null, resourcePath)) {
				// second check emulated
				for (String emulPackage: this.getModulesBySuperSourcePackage().keySet()) {
					String fullEmulationPath = emulPackage.replace('.', '/') + "/" + resourcePath;
					if (scanTopLevelClass(topLevelClassName, emulPackage, fullEmulationPath))
						break;
				}
			}
		}
		finally {
			scannedTopLevelClasses.add(topLevelClassName);
		}
	}
	
	private boolean scanTopLevelClass(String className, String superSourcePackage, String topLevelClassResource) {
		try {
			URL sourceUrl = getUrlClassLoader().getResource(topLevelClassResource);
			
			if (sourceUrl != null) {
				boolean found = false;
				if (superSourcePackage != null) {
					for (GwtModule module: getModulesBySuperSourcePackage().get(superSourcePackage)) {
						requiredModules.add(module);
						found = true;
					}
				}
				else {
					SortedSet<String> headSet = getModulesBySourcePackage().keySet().headSet(className);
					if (!headSet.isEmpty()) {
						String candidate = headSet.last();
						if (className.startsWith(candidate)) {
							for (GwtModule module: getModulesBySourcePackage().get(candidate)) {
								requiredModules.add(module);
								found = true;
							}
						}
					}
				}
				return found;
			}
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}
}
