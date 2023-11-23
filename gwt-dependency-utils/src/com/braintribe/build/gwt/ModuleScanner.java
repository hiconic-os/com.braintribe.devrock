// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.braintribe.logging.Logger;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

public class ModuleScanner {
	private List<File> classpath;
	private SortedMap<String, GwtModule> modulesByName = new TreeMap<String, GwtModule>();
	private TreeMultimap<String, GwtModule> modulesBySourcePackage = TreeMultimap.create();
	private TreeMultimap<String, GwtModule> modulesBySuperSourcePackage = TreeMultimap.create();
	private static final String moduleSuffix = ".gwt.xml";
	protected DocumentBuilder documentBuilder;
	private Monitor monitor = null;
	private static Logger logger = Logger.getLogger(ModuleScanner.class);
	private Set<String> clientPackages = new HashSet<String>();

	
	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}

	public ModuleScanner() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		documentBuilder = factory.newDocumentBuilder();
	}
	
	public TreeMultimap<String, GwtModule> getModulesBySourcePackage() {
		return modulesBySourcePackage;
	}
	
	public TreeMultimap<String, GwtModule> getModulesBySuperSourcePackage() {
		return modulesBySuperSourcePackage;
	}
	
	public void setClasspath(List<File> classpath) {
		this.classpath = classpath;
	}
	
	public List<File> getClasspath() {
		return classpath;
	}
	
	public void scanForModules() throws Exception {
		for (File file: classpath) {
			if (file.isDirectory()) {
				scanFolder(file);
			}
			else {
				scanJar(file);
			}
		}
		
		reduceModules();
	}
	
	protected void reduceModules() {
		reduceModules(this.modulesBySourcePackage);
		reduceModules(this.modulesBySuperSourcePackage);
	}
	
	protected void reduceModules(SortedSetMultimap<String, GwtModule> moduleMap) {
		Iterator<GwtModule> iterator = moduleMap.values().iterator();
		while (iterator.hasNext()) {
			GwtModule module = iterator.next();
			String sourcePackage = module.getSourcePackage();
			if (sourcePackage != null && sourcePackage.endsWith(".client") && !clientPackages.contains(sourcePackage))
				iterator.remove();
		}
		
		for (String packageName: new HashSet<String>(moduleMap.keySet())) {
			SortedSet<GwtModule> modules = moduleMap.get(packageName);
			if (modules.size() > 1) {
				
				Set<GwtModule> preferredModules = new HashSet<GwtModule>();
				
				for (GwtModule module: modules) {
					if (module.isExplicitSourcePackage()) {
						preferredModules.add(module);
					}
				}
				
				if (preferredModules.size() == 1) {
					modules.clear();
					modules.add(preferredModules.iterator().next());
					//logger.info("multiple module definitions for the same package: " + packageName + " reduced to explicit module");
				}
				else {
					Multimap<GwtModule, GwtModule> parentMap = HashMultimap.create();
					
					for (GwtModule module: modules) {
						for (GwtModule inheritedModule: module.getInheritedModules()) {
							if (modules.contains(inheritedModule))
								parentMap.put(inheritedModule, module);
						}
					}
					
					Iterator<GwtModule> moduleIterator = modules.iterator();
					while (moduleIterator.hasNext()) {
						GwtModule module = moduleIterator.next();
						if (!parentMap.get(module).isEmpty())
							moduleIterator.remove();
					}
					//logger.info("multiple module definitions for the same package: " + packageName + " reduced to top level parents");
				}
				
				if (modules.size() > 1) 
					logger.info("multiple module definitions for the same package: " + packageName);
				
			}
		}
	}
	
	protected void reduceModulesOld(SortedSetMultimap<String, GwtModule> moduleMap) {
		for (String packageName: moduleMap.keySet()) {
			SortedSet<GwtModule> modules = moduleMap.get(packageName);
			if (modules.size() > 1) {
				Multimap<GwtModule, GwtModule> parentMap = HashMultimap.create();
				
				for (GwtModule module: modules) {
					for (GwtModule inheritedModule: module.getInheritedModules()) {
						if (modules.contains(inheritedModule))
							parentMap.put(inheritedModule, module);
					}
				}
				
				Iterator<GwtModule> moduleIterator = modules.iterator();
				while (moduleIterator.hasNext()) {
					GwtModule module = moduleIterator.next();
					if (!parentMap.get(module).isEmpty())
						moduleIterator.remove();
				}
			}
		}
	}
	
	protected void scanJar(File file) throws IOException {
		JarFile jarFile = new JarFile(file);
		Enumeration<JarEntry> enumeration = jarFile.entries();
		
		while (enumeration.hasMoreElements()) {
			JarEntry jarEntry = enumeration.nextElement();
			String name = jarEntry.getName();
			
			if (name.endsWith("client/")) {
				String clientPackageName = name.replace('/', '.').substring(0, name.length() - 1);
				clientPackages.add(clientPackageName);
			}

			
			if (name.endsWith(moduleSuffix)) {
				String moduleName = name.substring(0, name.length() - moduleSuffix.length()).replace('/', '.');
				if (monitor != null) {
					monitor.acknowledgeModule(moduleName);
				}
				InputStream in = jarFile.getInputStream(jarEntry);
				
				try {
					scanInputStream(in, moduleName);
				}
				finally {
					in.close();
				}
			}
		}
	}
	
	protected void scanFolder(File folder) throws IOException {
		scanFolder(folder, new Stack<String>());
	}
	
	protected void scanFolder(File folder, Stack<String> packageLevels) throws IOException {
		for (File file: folder.listFiles()) {
			String fileName = file.getName();
			if (Arrays.asList("..", ".").contains(fileName))
				continue;
			
			if (file.isDirectory()) {
				if (file.getName().equals("client")) {
					StringBuilder packageNameBuilder = new StringBuilder();
					
					for (String packageLevel: packageLevels) {
						packageNameBuilder.append(packageLevel);
						packageNameBuilder.append('.');
					}
					
					packageNameBuilder.append(file.getName());

					clientPackages.add(packageNameBuilder.toString());
				}
				try {
					packageLevels.push(file.getName());
					scanFolder(file, packageLevels);
				}
				finally {
					packageLevels.pop();
				}
			}
			else if (fileName.endsWith(moduleSuffix)){
				InputStream in = new FileInputStream(file);
				StringBuilder moduleNameBuilder = new StringBuilder();
				
				for (String packageLevel: packageLevels) {
					moduleNameBuilder.append(packageLevel);
					moduleNameBuilder.append('.');
				}
				String simpleModuleName = fileName.substring(0, fileName.length() - moduleSuffix.length());
				moduleNameBuilder.append(simpleModuleName);
				String qualifiedModuleName = moduleNameBuilder.toString();
				
				try {
					scanInputStream(in, qualifiedModuleName);
				}
				finally {
					in.close();
				}
			}
		}
	}
	
	protected void scanInputStream(InputStream in, String moduleName) throws IOException {
		try {
			Document document = documentBuilder.parse(in);
			
			int indexOfPoint = moduleName.lastIndexOf('.');
			if (indexOfPoint <0) {
				throw new IOException("error while parsing gwt xml for module [" + moduleName + "]. Cannot derive base package. Is the file at the wrong place?");
			}
			String basePackage = moduleName.substring(0, indexOfPoint);
			
			GwtModule module = aquireModule(moduleName);
			module.setDocument(document);
			
			NodeList sourceElements = document.getElementsByTagName("source");
			NodeList superSourceElements = document
					.getElementsByTagName("super-source");
			NodeList inheritsElements = document.getElementsByTagName("inherits");
			
			
			String sourcePath = "client";
			
			for (int i = 0; i < superSourceElements.getLength(); i++) {
				Element superSourceElement = (Element) superSourceElements.item(i);
				String path = superSourceElement.getAttribute("path").replace('/', '.');
				
				String superSourcePackage = basePackage;
				if (path != null && path.length() != 0) {
					superSourcePackage = basePackage + "." + path; 
				}
				else {
					sourcePath = null;
				}

				modulesBySuperSourcePackage.put(superSourcePackage, module);
			}

			for (int i = 0; i < sourceElements.getLength(); i++) {
				Element sourceElement = (Element) sourceElements.item(i);
				String path = sourceElement.getAttribute("path").replace('/', '.');
				
				sourcePath = path;
				
				module.setExplicitSourcePackage(true);
			}

			if (sourcePath != null) {
				String sourcePackage = basePackage + "." + sourcePath;
				module.setSourcePackage(sourcePackage);
				modulesBySourcePackage.put(sourcePackage, module);
			}


			for (int i = 0; i < inheritsElements.getLength(); i++) {
				Element inheritsElement = (Element) inheritsElements.item(i);
				String inheritedModuleName = inheritsElement.getAttribute("name");
				GwtModule inheritedModule = aquireModule(inheritedModuleName);
				module.getInheritedModules().add(inheritedModule);
			}
		} catch (Exception e) {
			throw new IOException("error while parsing gwt xml for module " + moduleName, e);
		}
	}
	
	protected GwtModule aquireModule(String moduleName) {
		GwtModule module = modulesByName.get(moduleName);
		
		if (module == null) {
			module = new GwtModule(moduleName);
			modulesByName.put(moduleName, module);
		}
		
		return module;
	}
}
