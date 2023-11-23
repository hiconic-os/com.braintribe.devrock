// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.TypeDeclaration;

public class GwtAvailabilityCollector {
	private NavigableMap<String, String> sourcePackages = new TreeMap<String, String>();
	private SortedSet<String> superSourcePackages = new TreeSet<String>();
	private Set<String> visitedModules = new HashSet<String>();
	private Map<String, ModuleClassDependency> availability = new HashMap<String, ModuleClassDependency>();
	private Set<String> scannedTopLevelClasses = new HashSet<String>();
	private URLClassLoader urlClassLoader;
	private DocumentBuilder documentBuilder;
	private boolean strict;
	private Monitor monitor;
	
	public GwtAvailabilityCollector(URLClassLoader classLoader, boolean strict) throws ParserConfigurationException {
		this.urlClassLoader = classLoader;
		this.strict = strict;
	
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		documentBuilder = factory.newDocumentBuilder();
	}

	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}
	
	public void collectFor(String gwtModule) throws Exception {
		collectFor(gwtModule, 0);
	}
	
	protected void collectFor(String gwtModule, int depth) throws Exception {
		if (visitedModules.contains(gwtModule))
			return;
		else {
			visitedModules.add(gwtModule);		
		}
		if (monitor != null) {
			monitor.acknowledgeStep( "collecting for module [" + gwtModule + "]", 1);
		}

		String basePackage = gwtModule.substring(0, gwtModule.lastIndexOf('.'));
		String resourcePath = gwtModule.replace('.', '/') + ".gwt.xml";
		URL gwtXmlUrl = urlClassLoader.getResource(resourcePath);
		
		if (gwtXmlUrl == null)
			return;
		
		Document document = documentBuilder.parse(gwtXmlUrl.toURI()
				.toASCIIString());

		NodeList sourceElements = document.getElementsByTagName("source");
		NodeList superSourceElements = document
				.getElementsByTagName("super-source");
		NodeList inheritsElements = document.getElementsByTagName("inherits");

		boolean useDefaultSourcePath = true;
		
		for (int i = 0; i < superSourceElements.getLength(); i++) {
			Element superSourceElement = (Element) superSourceElements.item(i);
			String path = superSourceElement.getAttribute("path");
			
			if (path != null && path.length() != 0) {
				superSourcePackages.add(basePackage + "."
						+ path);
			}
			else {
				superSourcePackages.add(basePackage);
				sourcePackages.put(basePackage, gwtModule);
				useDefaultSourcePath = false;
			}
		}

		for (int i = 0; i < sourceElements.getLength(); i++) {
			Element sourceElement = (Element) sourceElements.item(i);
			String path = sourceElement.getAttribute("path");
			sourcePackages.put(basePackage + "."
					+ path.replace('/', '.'), gwtModule);
			useDefaultSourcePath = false;
		}

		if (useDefaultSourcePath)
			sourcePackages.put(basePackage + ".client", gwtModule);


		if (!(strict && depth == 1)) {
			for (int i = 0; i < inheritsElements.getLength(); i++) {
				Element inheritsElement = (Element) inheritsElements.item(i);
				collectFor(inheritsElement.getAttribute("name"), depth + 1);
			}
		}
	}
	
	public SortedMap<String, ModuleClassDependency> getUnavailables(Iterable<String> classNames) {
		SortedMap<String, ModuleClassDependency> unavailables = new TreeMap<String, ModuleClassDependency>();
		
		for (String className: classNames) {
			if (monitor != null) {
				monitor.acknowledgeStep(className, 1);
			}
			ModuleClassDependency availability = getAvailability(className);
			if (availability.getInheritedFrom() == null) {
				unavailables.put(className, availability);
			}
		}
		
		return unavailables;
	}

	public ModuleClassDependency getAvailability(String className) {
		ModuleClassDependency availability = this.availability.get(className);
		
		if (availability == null) {
			int index = className.lastIndexOf('.');
			String packageName = className.substring(0, index);
			String classDeclarationName = className.substring(index + 1);
			
			index = classDeclarationName.indexOf('$');
			
			String topLevelSimpleClassName = index == -1? classDeclarationName: classDeclarationName.substring(0, index);
			String topLevelClassName = packageName + '.' + topLevelSimpleClassName;
			
			ensureScanned(topLevelClassName);
			
			availability = this.availability.get(className);
	
			if (availability == null) {
				availability = new ModuleClassDependency();
				availability.setClassName(className);
				this.availability.put(className, availability);
			}
			
		}

		return availability;
	}
	
	private void ensureScanned(String topLevelClassName) {
		if (scannedTopLevelClasses.contains(topLevelClassName))
			return;

		try {
			// first check non emulated
			String resourcePath = topLevelClassName.replace('.', '/') + ".java";
			if (!scanTopLevelClass(null, resourcePath)) {
				// second check emulated
				for (String emulPackage: superSourcePackages) {
					String fullEmulationPath = emulPackage.replace('.', '/') + "/" + resourcePath;
					if (scanTopLevelClass(emulPackage, fullEmulationPath))
						break;
				}
			}
		}
		finally {
			scannedTopLevelClasses.add(topLevelClassName);
		}
	}
	
	
	
	private boolean scanTopLevelClass(String superSourcePackage, String topLevelClassResource) {
		try {
			URL sourceUrl = urlClassLoader.getResource(topLevelClassResource);
			
			if (sourceUrl == null)
				return false;

			InputStream in = sourceUrl.openStream();
			CompilationUnit cu;
	        try {
	            // parse the file
	            cu = JavaParser.parse(in);
	        } finally {
	            in.close();
	        }
	        
	        for (TypeDeclaration typeDeclaration: cu.getTypes()) {
	        	traverseTypeDeclaration(topLevelClassResource, superSourcePackage, cu.getPackage().getName().toString(), typeDeclaration, new Stack<TypeDeclaration>());
	        }
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	protected void traverseTypeDeclaration(String pathToResource, String superSourcePackage, String packageName, TypeDeclaration typeDeclaration,
			Stack<TypeDeclaration> declaringTypes) {
		StringBuilder className = new StringBuilder();
		className.append(packageName);
		className.append('.');

		for (TypeDeclaration parentType: declaringTypes) {
			className.append(parentType.getName());
			className.append('$');
		}

		className.append(typeDeclaration.getName());
		String classNameStr = className.toString();
		ModuleClassDependency availability = new ModuleClassDependency();
		availability.setClassName(classNameStr);

		if (superSourcePackage != null) {
			SortedMap<String, String> headMap = this.sourcePackages.headMap(superSourcePackage, true);
			String sourcePackage = headMap.lastKey();
			String module = headMap.get(sourcePackage);
			availability.setInheritedFrom(module);
			availability.setEmulation(true);
		}
		else {
			SortedMap<String, String> headMap = this.sourcePackages.headMap(classNameStr);
			if (!headMap.isEmpty()) {
				String candidate = headMap.lastKey();
				if (classNameStr.startsWith(candidate)) {
					String module = headMap.get(candidate);
					availability.setInheritedFrom(module);
				}
			}
		}
		
		availability.setPathToSource(pathToResource);
				
		this.availability.put(classNameStr, availability);		
		try {
			declaringTypes.push(typeDeclaration);
			List<BodyDeclaration> members = typeDeclaration.getMembers();
			if (members != null) {
				for (BodyDeclaration member: members) {
					if (member instanceof TypeDeclaration) {
						TypeDeclaration memberType = (TypeDeclaration) member;
						traverseTypeDeclaration(pathToResource, superSourcePackage, packageName, memberType, declaringTypes);
					}
				}
			}
		} finally {
			declaringTypes.pop();
		}
	}
	

}
