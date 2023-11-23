// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.braintribe.logging.Logger;

public class GwtModuleChecker {
	private File artifactClassFolder;
	private List<File> classpath;
	private File srcPath;
	private boolean strict = false;
	private Monitor monitor = null;
	
	
	private static final Logger log = Logger.getLogger(GwtModuleChecker.class);
	
	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	
	public void setSrcPath(File srcPath) {
		this.srcPath = srcPath;
	}
	
	public void setArtifactClassFolder(File artifactClassFolder) {
		this.artifactClassFolder = artifactClassFolder;
	}
	
	
	public void setClasspath(List<File> classpath) {
		this.classpath = classpath;
	}
	
	protected String getClassNameFromClassFile(File classFile) {
		return getModuleOrClassNameFromClassFile(artifactClassFolder, classFile, ".class");
	}
	
	protected String getModuleNameFromModuleFile(File moduleFile) {
		return getModuleOrClassNameFromClassFile(srcPath, moduleFile, ".gwt.xml");
	}
	
	protected String getModuleOrClassNameFromClassFile(File rootFolder, File file, String suffix) {
		String basePath = rootFolder.getPath();
		String fullPath = file.getPath();
		String relativePath = fullPath.substring(basePath.length() + 1);
		String className = relativePath.replace(File.separatorChar, '.').substring(0, relativePath.length() - suffix.length());
		return className;
	}
	
	public Set<ModuleCheckProtocol> check() throws Exception {
		URL urls[] = new URL[classpath.size() + 1];

		urls[0] = srcPath.toURI().toURL();
		
		for (int i = 0; i < classpath.size(); i++) {
			URL url = classpath.get(i).toURI().toURL();
			urls[i + 1] = url;
		}
		
		URLClassLoader classLoader = new URLClassLoader(urls, null);
		
		Set<String> moduleNames = scanForModules();
		
		Set<ModuleCheckProtocol> moduleCheckProtocols = new HashSet<ModuleCheckProtocol>();
		
		for (String moduleName: moduleNames) {
			
			ModuleCheckProtocolImpl checkProtocolImpl = new ModuleCheckProtocolImpl();
			checkProtocolImpl.setModuleName(moduleName);
			/*
			if (monitor != null) {
				monitor.acknowledgeStep( "scanning module : " + moduleName, 1);
			}
			*/
			List<File> classFiles = getModuleClassFiles(moduleName);
			Set<String> allClasses = new HashSet<String>();
					
			if (monitor != null) {
				monitor.acknowledgeTask( "scanning modules", classFiles.size());
			}
			for (File classFile: classFiles) {
				Set<String> dependencies = AsmClassDepScanner.getClassDependencies(classFile);
				
				filterAnonymousInnerClasses(dependencies);
				String className = getClassNameFromClassFile(classFile);
				if (monitor != null) {
					monitor.acknowledgeStep("scanning " + className, 1);			
				}
				checkProtocolImpl.registerClassDependencies(className, dependencies);
				allClasses.addAll(dependencies);
			}			
									
			GwtAvailabilityCollector collector = new GwtAvailabilityCollector(classLoader, strict);			
			collector.collectFor(moduleName);
			collector.setMonitor(monitor);
			
			if (monitor != null) {
				monitor.acknowledgeTask( "retrieving unavailables", allClasses.size());
			}
			SortedMap<String, ModuleClassDependency> unavailables = collector.getUnavailables(allClasses);
			
			/*for (Map.Entry<String,ModuleClassDependency> entry: unavailables.entrySet()) {
				ModuleClassDependency availability = entry.getValue();
				System.out.println(entry.getKey() + " -> pathToSource=" + availability.getPathToSource());
			}*/
			
			checkProtocolImpl.addAll(unavailables.values());
			
			moduleCheckProtocols.add(checkProtocolImpl);
		}
		if (monitor != null) {
			monitor.done();
		}
		
		return moduleCheckProtocols;
	}
	
	protected void filterAnonymousInnerClasses(Set<String> dependencies) {
		Iterator<String> it = dependencies.iterator();
		
		while (it.hasNext()) {
			String className = it.next();
			
			String classNames[] = className.split("\\$");
			
			if (classNames.length > 1) { 
				for (String name : classNames) {
					if (!Character.isJavaIdentifierStart(name.charAt(0))) {
						it.remove();
						break;
					}				
				}
			}
		}
	}

	protected Set<String> scanForModules() {
		Set<String> modules = new HashSet<String>();
		collectModules(srcPath, modules);
		return modules;
	}
	
	protected void collectModules(File folder, Set<String> modules) {
		for (File file: folder.listFiles()) {
			if (file.getName().toLowerCase().endsWith(".gwt.xml")) {
				String moduleName = getModuleNameFromModuleFile(file);
				// add monitor callback.. 
				if (monitor != null) {
					monitor.acknowledgeModule( moduleName);
				}
				modules.add(moduleName);
			}
			
			if (file.isDirectory() && !(file.getName().equals(".") || file.getName().equals(".."))) {
				collectModules(file, modules);
			}
		}
	}

	protected List<File> getModuleClassFiles(String moduleName) throws Exception {
		File moduleRootFolder = getModuleRootFolder(moduleName);
		
		List<File> classFiles = new LinkedList<File>();
		collectAllClassFiles(moduleRootFolder, classFiles);
		return classFiles;
	}
	
	protected void collectAllClassFiles(File folder, List<File> files) {
		if (!folder.isDirectory()) {
			log.warn("Not an existing directory: " + folder.getAbsolutePath());
			return;
		}
		
		for (File file: folder.listFiles()) {
			if (file.getName().toLowerCase().endsWith(".class"))
				files.add(file);
			if (file.isDirectory() && !(file.getName().equals(".") || file.getName().equals(".."))) {
				collectAllClassFiles(file, files);
			}
		}
	}
	
	protected File getModuleRootFolder(String moduleName) throws Exception {
		File srcMmoduleFile = new File(srcPath, moduleName.replace('.', '/') + ".gwt.xml");
		File classesModuleFile = new File(artifactClassFolder, moduleName.replace('.', '/') + ".gwt.xml");
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
}
