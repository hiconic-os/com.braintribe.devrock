// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.model;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifacts.mc.wire.pomreader.PomReaderSetup;
import com.braintribe.build.artifacts.mc.wire.pomreader.PomReaders;
import com.braintribe.build.model.entity.Entity;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Property;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.utils.CommonTools;


/**
 * 
 * @author pit (adaption to allow alternative to classloader based reflection) 
 */
public class ModelDeclarations {
	private static final String JAVA_CLASS_EXT = ".class";


	public static void main(String[] args) {
		UpdateModelDeclaration options = OptionParser.parse(UpdateModelDeclaration.T, args);
		buildModelDeclaration(options, true);
	}
		
	public static void buildModelDeclaration(UpdateModelDeclaration options) {
		buildModelDeclaration(options, false);
	}		
	public static void buildModelDeclaration(UpdateModelDeclaration options, boolean useClassLoaderReflection) {
		
		List<File> buildFolders = options.getBuildFolders().stream().map(File::new).collect(Collectors.toList());
		
		String targetFolderAsStr = options.getTargetFolder();
		
		File targetFolder = buildFolders.get(0);
		
		if (targetFolderAsStr != null) {
			targetFolder = new File(targetFolderAsStr);
		}
		
		File pomFile = new File(options.getPomFile());
		
		List<URL> cp = Stream.of(options.getClasspath().split(File.pathSeparator)).map(ModelDeclarations::toUrl).collect(Collectors.toList());
		
		buildModelDeclaration(cp, buildFolders, pomFile, targetFolder, useClassLoaderReflection);	
	}
	
	private static URL toUrl(String s) {
		try {
			return new File(s).toURI().toURL();
		} catch (MalformedURLException e) {
			throw Exceptions.unchecked(e, "Error while building file URL from string: " + s, IllegalArgumentException::new);
		}
	}
	
	public static void buildModelDeclaration(List<URL> cp, List<File> buildFolders, File pomFile, File targetFolder) {
		buildModelDeclaration(cp, buildFolders, pomFile, targetFolder, false);
	}		
	public static void buildModelDeclaration(List<URL> cp, List<File> buildFolders, File pomFile, File targetFolder, boolean useClassLoaderReflection) {
		try (PomReaderSetup pomReaderSetup = PomReaders.build().done()) {
			buildModelDeclaration(pomReaderSetup.pomReader(), cp, buildFolders, pomFile, targetFolder, useClassLoaderReflection);
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while setting up pom reader", IllegalStateException::new);
		}
	}
	
	public static void buildModelDeclaration(ArtifactPomReader pomReader, List<URL> cp, List<File> buildFolders, File pomFile, File targetFolder) {
		buildModelDeclaration(pomReader, cp, buildFolders, pomFile, targetFolder, false);
	}	
	public static void buildModelDeclaration(ArtifactPomReader pomReader, List<URL> cp, List<File> buildFolders, File pomFile, File targetFolder, boolean useClassLoaderReflection) {
		try {
			ModelDescriptor modelDescriptor = buildModelDescriptor(pomReader, pomFile);
			
			URLClassLoader classLoader = new URLClassLoader(cp.toArray(new URL[cp.size()]), null);
			
			Map<String, File> classes= new TreeMap<>();
			
			for (File scanFolder: buildFolders)
				scanTypeCandidates(null, scanFolder, classes);
			
			filterTypeCandidates(classLoader, classes.keySet(), modelDescriptor.declaredTypes, modelDescriptor.forwardTypes, useClassLoaderReflection);
			
			Collection<File> sortedClassFiles = createSortedClassFiles(classes);
			
			modelDescriptor.hash = buildHash(Stream.concat(Stream.of(pomFile), sortedClassFiles.stream()));
			
			Map<String, Set<String>> forwards = scanForwards(classLoader);
			
			Set<String> forwardTypes = forwards.get(modelDescriptor.name);
			
			if (forwardTypes != null) {
				modelDescriptor.declaredTypes.addAll(forwardTypes);
			}
			
			if (!modelDescriptor.artifactId.equals("GmCoreApi")) {
				writeDescriptor(new File(targetFolder, "model-declaration.xml"), modelDescriptor);
			}
			else {
				writeForwardDescriptor(new File(targetFolder, "model-forward-declaration.xml"), modelDescriptor);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Collection<File> createSortedClassFiles(Map<String, File> values) {
		SortedMap<String, File> sortedFiles = new TreeMap<>(values);
		return sortedFiles.values();
	}

	public static String buildHash(Stream<File> fileStream) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			
			byte buffer[] = new byte[0xffff];
			
			fileStream.forEach(file -> {
				try {
					try (InputStream in = new FileInputStream(file)) {
						int bytes = 0;
						while ((bytes = in.read(buffer)) != -1) {
							digest.update(buffer, 0, bytes);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException("failed to hash for " + file);
				} 
			});
			
			byte[] hashBytes = digest.digest();
			
			return CommonTools.printHexBinary(hashBytes).toLowerCase();
		} catch (Exception e) {
			throw new GenericModelException("error while building hash");
		}
	}

	private static Map<String, Set<String>> scanForwards(URLClassLoader classLoader) throws Exception {
		Enumeration<URL> urls = classLoader.getResources("model-forward-declaration.xml");
		
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			
			readModelForwardDeclaration(url, map);
		}
		
		return map;
	}
	
	private static void readModelForwardDeclaration(URL url, Map<String, Set<String>> map) throws Exception {
		try (InputStream in = url.openStream()) {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
			
			Element documentElement = doc.getDocumentElement();
			
			for (Node node = documentElement.getFirstChild(); node != null; node = node.getNextSibling()) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element)node;
					if (element.getTagName().equals("for-model")) {
						String modelName = element.getAttribute("name");
						
						Set<String> forwardTypes = map.get(modelName);
						
						if (forwardTypes == null) {
							forwardTypes = new HashSet<String>();
							map.put(modelName, forwardTypes);
						}
						
						for (Node subNode = element.getFirstChild(); subNode != null; subNode = subNode.getNextSibling()) {
							
							if (subNode.getNodeType() == Node.ELEMENT_NODE) {
								Element subElement = (Element)subNode;
								if (subElement.getTagName().equals("type")) {
									String type = subElement.getTextContent();
									forwardTypes.add(type);
								}
							}
						}
					}
				}
			}
		}
	}

	private static void writeForwardDescriptor(File file, ModelDescriptor modelDescriptor) throws Exception {
		if (modelDescriptor.forwardTypes.isEmpty())
			return;
		
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			writer.write("<model-forward-declaration>\n\n");
			
			for (Map.Entry<String, Set<String>> entry: modelDescriptor.forwardTypes.entrySet()) {
				writer.write("  <for-model name='" + entry.getKey() + "'>\n");
				for (String type: entry.getValue()) {
					writer.write("    <type>"); writer.write(type); writer.write("</type>\n");
				}
				writer.write("  </for-model>\n\n");
			}
			
			writer.write("</model-forward-declaration>");
			
		}
	}
	

	public static void writeDescriptor(File file, ModelDescriptor modelDescriptor) throws Exception {
		writeDescriptor(new FileOutputStream(file), modelDescriptor);
	}
	
	public static void writeDescriptor(OutputStream out, ModelDescriptor modelDescriptor) throws Exception {
		OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
		writeDescriptor(writer, modelDescriptor);
		writer.flush();
	}
	
	public static void writeDescriptor(Writer writer, ModelDescriptor modelDescriptor) throws Exception {
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		writer.write("<model-declaration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"model-declaration-1.0.xsd\">\n\n");
		
		writer.write("  <name>"); writer.write(modelDescriptor.name); writer.write("</name>\n\n");
		writer.write("  <groupId>"); writer.write(modelDescriptor.groupId); writer.write("</groupId>\n");
		writer.write("  <artifactId>"); writer.write(modelDescriptor.artifactId); writer.write("</artifactId>\n");
		writer.write("  <version>"); writer.write(modelDescriptor.version); writer.write("</version>\n");
		if (modelDescriptor.globalId != null) {
			writer.write("  <globalId>"); writer.write(modelDescriptor.globalId); writer.write("</globalId>\n");
		}
		writer.write("  <hash>"); writer.write(modelDescriptor.hash); writer.write("</hash>\n\n");
		
		writer.write("  <dependencies>\n");
		for (String dependency: modelDescriptor.dependencies) {
			writer.write("    <dependency>"); writer.write(dependency); writer.write("</dependency>\n");
		}
		writer.write("  </dependencies>\n\n");
		
		writer.write("  <types>\n");
		for (String type: modelDescriptor.declaredTypes) {
			writer.write("    <type>"); writer.write(type); writer.write("</type>\n");
		}
		writer.write("  </types>\n\n");
		
		writer.write("</model-declaration>");
	}
	
	private static ModelDescriptor buildModelDescriptor(ArtifactPomReader pomReader, File file) throws Exception {
		String uuid = UUID.randomUUID().toString();
		Solution pomInfo = pomReader.readPom(uuid, file);
		
		ModelDescriptor modelDescriptor = new ModelDescriptor();
		
		modelDescriptor.groupId = pomInfo.getGroupId();
		modelDescriptor.artifactId = pomInfo.getArtifactId();
		modelDescriptor.version = VersionProcessor.toString(pomInfo.getVersion());
		modelDescriptor.globalId = pomInfo.getProperties().stream().filter(p -> p.getName().equals("globalId")).map(Property::getValue).findFirst().orElse(null);
		modelDescriptor.name = modelDescriptor.groupId + ":" + modelDescriptor.artifactId;
		
		for (Dependency dependency: pomInfo.getDependencies()) {
			if (isModelDependency(dependency)) {
				String modelName = dependency.getGroupId() + ":" + dependency.getArtifactId();
				modelDescriptor.dependencies.add(modelName);
			}
		}
		
		return modelDescriptor;
	}
	
	private static boolean isModelDependency(Dependency dependency) {
		return !dependency.getArtifactId().equals("GmCoreApi") && !isFunctionalDependency(dependency);
	}

	private static boolean isFunctionalDependency(Dependency dependency) {
		return "functional".equals(dependency.getGroup());
	}

	public static class ModelDescriptor {
		public String groupId;
		public String artifactId;
		public String version;
		public String globalId;
		public String name;
		public String hash;
		public Set<String> declaredTypes = new TreeSet<String>();
		public List<String> dependencies = new ArrayList<String>();
		public Map<String, Set<String>> forwardTypes = new TreeMap<String, Set<String>>();
	}
	
	
	// TODO: think about the impact of classloading here since the tools runs in a different classloader and maybe even GmCore
	// findings so far: we need a complete separate classloader with the complete dependencies from the model and need to check assignability and annotations
	// with classes from that classloader (GenericEntity, ForwardDeclaration)
	private static void filterTypeCandidates(ClassLoader classLoader, Collection<String> classNames, Set<String> declaredTypes, Map<String, Set<String>> forwardTypes, boolean classloaderReflection) {
		
		ModelReflection tools;
		if (classloaderReflection) {		
			tools = ModelClassLoaderReflection.scan(classLoader);
		}
		else {
			tools = ModelAsmReflection.scan( classLoader);
		}
		
		for (String className: classNames) {
			Entity entity = tools.load(className);
			if (entity == null) {
				System.out.println("no matching entity found for:" + className);
				continue;
			}
			if (entity.getIsEnum() || entity.getIsGenericEntity()) {
				String forwardModel = entity.getForwardDeclaration();				
				if (forwardModel == null) {
					declaredTypes.add(className);
				}
				else {
					Set<String> modelForwardTypes = forwardTypes.get(forwardModel);
					if (modelForwardTypes == null) {
						modelForwardTypes = new TreeSet<String>();
						forwardTypes.put(forwardModel, modelForwardTypes);
					}
					modelForwardTypes.add(className);
				}
			}
		}
	}
	
 
	
	private static void scanTypeCandidates(String path, File folder, Map<String, File> classes) {
		
		for (File file: folder.listFiles()) {
			String fileName = file.getName();
			if (file.isDirectory()) {
				scanTypeCandidates(concat(path, fileName), file, classes);
			}
			else if (fileName.endsWith(JAVA_CLASS_EXT)) {
				String plainName = fileName.substring(0, fileName.length() - JAVA_CLASS_EXT.length());
				String className = concat(path, plainName);
				classes.put(className, file);
			}
		}
	}

	private static String concat(String path, String name) {
		return path == null? name: path + "." + name;
	}
}
