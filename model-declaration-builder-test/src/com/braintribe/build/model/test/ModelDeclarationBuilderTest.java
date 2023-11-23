// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.model.test;


import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.build.model.ModelDeclarations;
import com.braintribe.devrock.mc.core.compiled.ArtifactCompiler;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.paths.PathCollectors;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;

@Category(KnownIssue.class)
public class ModelDeclarationBuilderTest {
	
	private static final String MODEL_DECLARATION_XML = "model-declaration.xml";
	
	@Test
	public void testWithClassLoaderReflection() {
		try {
			testModelDeclarationBuilder(true);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown: " + e.getMessage());			
		}
	}
	
	@Test
	public void testWithAsmReflection() {
		try {
			testModelDeclarationBuilder(false);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown: " + e.getMessage());			
		}
	}
	

	private Maybe<CompiledArtifact> readPom(File file) {
		return new ArtifactCompiler().compileReasoned(file);		
	}


	private void testModelDeclarationBuilder(boolean useClassLoaderReflection) throws Exception {
		File buildFolder = new File("build-snapshot");
		File pomFile = new File("test-pom.xml");
		List<File> buildFolders = Collections.singletonList(buildFolder);
		List<URL> cp = cp();
		cp.add(buildFolder.toURI().toURL());
		
		File targetFolder = new File("build-output");
		
		targetFolder.mkdirs();
		
		File targetFile = new File(targetFolder, MODEL_DECLARATION_XML);
		
		if (targetFile.exists())
			targetFile.delete();
		
		ModelDeclarations.buildModelDeclaration(this::readPom, cp, buildFolders, pomFile, targetFolder, useClassLoaderReflection);
		
		checkResult(targetFolder, 
				"com.braintribe.devrock", 
				"model-declaration-builder-test-model", 
				"1.0", 
				"com.braintribe.devrock:model-declaration-builder-test-model", 
				Arrays.asList("com.braintribe.model.mdbt.SomeEntity", "com.braintribe.model.mdbt.SomeEnum", "com.braintribe.model.mdbt.SomeExtendedEntity", "com.braintribe.model.mdbt.NewStyleEnum"),
				Arrays.asList("com.braintribe.gm:resource-model", "com.braintribe.gm:root-model")
		);
	}
	
	public void checkResult(File buildFolder, String groupId, String artifactId, String version, String name, List<String> types, List<String> modelDependencies) throws Exception {
		File modelDeclarationFile = new File(buildFolder, MODEL_DECLARATION_XML);
		Assert.assertTrue("expected file is missing: " + modelDeclarationFile, modelDeclarationFile.exists());
		
		URL schemaUrl = getClass().getClassLoader().getResource("com/braintribe/build/model/model-declaration.xsd");
		try (InputStream in = schemaUrl.openStream()) { 
			StringWriter capture = new StringWriter();
			boolean validate = DomParser.validate().from(modelDeclarationFile).schema( in).makeItSo( new StreamResult( capture));
			if (!validate) {
				Assert.fail("validation failed [" + capture.toString() + "]");
			}
		}
		
		Document doc = DomParser.load().from(modelDeclarationFile);
		
		
		//TODO: validate with schema
		
		Element element = doc.getDocumentElement();
		
		assertElementProperty(element, "groupId", groupId);
		assertElementProperty(element, "artifactId", artifactId);
		assertElementProperty(element, "version", version);
		assertElementProperty(element, "name", name);
		
		assertCollection(element, "types", "type", types);
		assertCollection(element, "dependencies", "dependency", modelDependencies);
	}
	
	private void assertCollection(Element parent, String collectionElementName, String valueElementName, Collection<String> expectedValues) {
		Element collectionElement = DomUtils.getElementByPath(parent, collectionElementName, false);
		Set<String> values = new HashSet<>();
		
		if (collectionElement != null) {
			Iterable<Element> iterable = () -> DomUtils.getElementIterator(collectionElement, valueElementName);
			for (Element element: iterable) {
				String content = element.getTextContent();
				values.add(content);
			}
		}
		
		Assert.assertTrue("the contents of all " + collectionElementName + "/" + valueElementName + " " + values + " where not matching the expectation " + expectedValues, values.containsAll(expectedValues) && values.size() == expectedValues.size());
	}
	
	private void assertElementProperty(Element parent, String path, String expectedValue) {
		String value = DomUtils.getElementValueByPath(parent, path, false);
		
		Assert.assertTrue("element " + path + " was not " + expectedValue + " yet [" + value + "]", expectedValue.equals(value));
	}
	
	List<URL> cp() {
		String urlString[] = {
	     PathCollectors.filePath.join("res", "GmCoreApi-1.2.jar"),
		};
		
		List<URL> cp = new ArrayList<>();
		
		try {
			for (String filePath: urlString) {
				File file = new File(filePath);
				URL url = file.toURI().toURL();
				cp.add(url);
			}
		} catch (MalformedURLException e) {
			throw Exceptions.unchecked(e, "url building", IllegalArgumentException::new);
		}
		
		return cp;
	}
}
