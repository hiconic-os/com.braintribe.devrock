// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.artifacts.test.name;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.name.NameParser.DeclarationMode;
import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;

public class NameTest {

	private String springContext = "spring-context-3.0.5.RELEASE.jar";
	private String springContext2 = "org.springframework:spring-context#3.0.5.RELEASE.jar"; 
	
	private String hibernate ="hibernate-core-3.5.4-FINAL.jar";
	private String hibernate2 = "org.hibernate:hibernate-core#3.5.4-FINAL.jar";
			
	private String metamodel="com.braintribe.model.MetaModel-1.0.jar";
	private String otherModel = "com.braintribe.model.MetaModel-1.0-SNAPSHOT";
	


	
	@Before
	public void setUp() throws Exception {

	}
	
	@Test
	public void testParser() {
		try {
			Part part = NameParser.parseCondensedName( springContext2);
			String name = NameParser.buildName( part);
			Assert.assertEquals("parse name not equals expected value", springContext2, name);
		} catch (NameParserException e) {
			Assert.fail("Exception [" + e + "] thrown");
		}
		
		try {
			Part part = NameParser.parseCondensedName( hibernate2);
			String name = NameParser.buildName( part);
			Assert.assertEquals("parse name not equals expected value", hibernate2, name);
		} catch (NameParserException e) {
			Assert.fail("Exception [" + e + "] thrown");
		}
		
	}
	
	@Test
	public void testNameInterpretion() {
		try {
			Part part = NameParser.parseSimpleCondensedName( metamodel);
			String name = NameParser.buildName( part);			
			System.out.println("Name is [" + name + "]");
			
			part = NameParser.parseSimpleCondensedName( otherModel);
			name = NameParser.buildName( part);			
			System.out.println("Name is [" + name + "]");
			
		} catch (NameParserException e) {
			Assert.fail("Exception [" + e + "] thrown");
		}				
	}
	
	@Test
	public void testNameParserWithClassifier() {
		try {
			Dependency dependency = Dependency.T.create();
			dependency.setGroupId( "net.sf.json-lib");
			dependency.setArtifactId( "json-lib");
			dependency.setVersionRange( VersionRangeProcessor.createFromString( "2.2.1"));
			dependency.setClassifier( "jdk15");
			
			Part pomPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "pom"));
			String pomName = NameParser.buildName(pomPart);
			System.out.println( pomName);
			Assert.assertEquals( "net.sf.json-lib:json-lib#2.2.1.pom", pomName);
			Part jarPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "jar"));
			String jarName = NameParser.buildName(jarPart);
			System.out.println( jarName);
			Assert.assertEquals( "net.sf.json-lib:json-lib#2.2.1-jdk15.jar", jarName);
			Part javaDocPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "javadoc:jar"));
			String javaDocName = NameParser.buildName(javaDocPart);
			System.out.println( javaDocName);
			Assert.assertEquals( "net.sf.json-lib:json-lib#2.2.1-jdk15-javadoc.jar", javaDocName);
			Part sourcesPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "sources:jar"));
			String sourcesName = NameParser.buildName(sourcesPart);
			System.out.println( sourcesName);
			Assert.assertEquals( "net.sf.json-lib:json-lib#2.2.1-jdk15-sources.jar", sourcesName);
			
		} catch (VersionProcessingException e) {
			Assert.fail("Exception [" + e + "] thrown");
		} 
	}
	
	@Test
	public void testExpressionWithClassifier() {
		try {
			Dependency dependency = Dependency.T.create();
			dependency.setGroupId( "net.sf.json-lib");
			dependency.setArtifactId( "json-lib");
			dependency.setVersionRange( VersionRangeProcessor.createFromString( "2.2.1"));
			dependency.setClassifier( "jdk15");
			
			Part pomPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "pom"));
			String pomName = NameParser.buildExpression( pomPart, "", DeclarationMode.MODE_DETAILED);
			System.out.println( pomName);
			Assert.assertEquals( "/net/sf/json-lib/json-lib/2.2.1/json-lib-2.2.1.pom", pomName);
			Part jarPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "jar"));
			String jarName = NameParser.buildExpression(jarPart, "", DeclarationMode.MODE_DETAILED);
			System.out.println( jarName);
			Assert.assertEquals( "/net/sf/json-lib/json-lib/2.2.1/json-lib-2.2.1-jdk15.jar", jarName);
			Part javaDocPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "javadoc:jar"));
			String javaDocName = NameParser.buildExpression( javaDocPart, "", DeclarationMode.MODE_DETAILED);
			System.out.println( javaDocName);
			Assert.assertEquals( "/net/sf/json-lib/json-lib/2.2.1/json-lib-2.2.1-jdk15-javadoc.jar", javaDocName);
			Part sourcesPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "sources:jar"));
			String sourcesName = NameParser.buildExpression(sourcesPart, "", DeclarationMode.MODE_DETAILED);
			System.out.println( sourcesName);
			Assert.assertEquals( "/net/sf/json-lib/json-lib/2.2.1/json-lib-2.2.1-jdk15-sources.jar", sourcesName);
			
		} catch (VersionProcessingException e) {
			Assert.fail("Exception [" + e + "] thrown");
		} 
	}
	
	@Test
	public void testExpressionWithoutClassifier() {
		try {
			Dependency dependency = Dependency.T.create();
			dependency.setGroupId( "net.sf.json-lib");
			dependency.setArtifactId( "json-lib");
			dependency.setVersionRange( VersionRangeProcessor.createFromString( "2.2.1"));
			dependency.setClassifier( "jdk15");
			
			Part pomPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "pom"));
			String pomName = NameParser.buildExpressionWithOutClassifier( pomPart, "", DeclarationMode.MODE_DETAILED);
			System.out.println( pomName);
			Assert.assertEquals( "/net/sf/json-lib/json-lib/2.2.1/json-lib-2.2.1.pom", pomName);
			Part jarPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "jar"));
			String jarName = NameParser.buildExpressionWithOutClassifier(jarPart, "", DeclarationMode.MODE_DETAILED);
			System.out.println( jarName);
			Assert.assertEquals( "/net/sf/json-lib/json-lib/2.2.1/json-lib-2.2.1.jar", jarName);
			Part javaDocPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "javadoc:jar"));
			String javaDocName = NameParser.buildExpressionWithOutClassifier( javaDocPart, "", DeclarationMode.MODE_DETAILED);
			System.out.println( javaDocName);
			Assert.assertEquals( "/net/sf/json-lib/json-lib/2.2.1/json-lib-2.2.1-javadoc.jar", javaDocName);
			Part sourcesPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "sources:jar"));
			String sourcesName = NameParser.buildExpressionWithOutClassifier(sourcesPart, "", DeclarationMode.MODE_DETAILED);
			System.out.println( sourcesName);
			Assert.assertEquals( "/net/sf/json-lib/json-lib/2.2.1/json-lib-2.2.1-sources.jar", sourcesName);
			
		} catch (VersionProcessingException e) {
			Assert.fail("Exception [" + e + "] thrown");
		} 
	}
	
	@Test
	public void testFilenameWithClassifier() {
		try {
			Dependency dependency = Dependency.T.create();
			dependency.setGroupId( "net.sf.json-lib");
			dependency.setArtifactId( "json-lib");
			dependency.setVersionRange( VersionRangeProcessor.createFromString( "2.2.1"));
			dependency.setClassifier( "jdk15");
			
			Part pomPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "pom"));
			String pomName = NameParser.buildFileName(pomPart);
			System.out.println( pomName);
			Assert.assertEquals( "json-lib-2.2.1.pom", pomName);
			Part jarPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "jar"));
			String jarName = NameParser.buildFileName(jarPart);
			System.out.println( jarName);
			Assert.assertEquals( "json-lib-2.2.1-jdk15.jar", jarName);
			Part javaDocPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "javadoc:jar"));
			String javaDocName = NameParser.buildFileName( javaDocPart);
			System.out.println( javaDocName);
			Assert.assertEquals( "json-lib-2.2.1-javadoc.jar", javaDocName);
			Part sourcesPart = ArtifactProcessor.createPartFromIdentification( dependency, VersionProcessor.createFromString( "2.2.1"), PartTupleProcessor.fromString( "sources:jar"));
			String sourcesName = NameParser.buildFileName(sourcesPart);
			System.out.println( sourcesName);
			Assert.assertEquals( "json-lib-2.2.1-sources.jar", sourcesName);
			
		} catch (VersionProcessingException e) {
			Assert.fail("Exception [" + e + "] thrown");
		} 
	}
	
	@Test
	public void testArtifactExtractionFromFile() {
		try {
			Artifact artifact = NameParser.parseFileName(springContext);
			String name = NameParser.buildName(artifact, artifact.getVersion());
			System.out.println( springContext + "->" + name);
			
			artifact = NameParser.parseFileName( hibernate);
			name = NameParser.buildName(artifact, artifact.getVersion());
			System.out.println( hibernate + "->" + name);
			
			artifact = NameParser.parseFileName( metamodel);
			name = NameParser.buildName(artifact, artifact.getVersion());
			System.out.println( metamodel + "->" + name);			
			
		} catch (Exception e) {
			Assert.fail("Exception [" + e + "] thrown");
		}
		
	}
}
