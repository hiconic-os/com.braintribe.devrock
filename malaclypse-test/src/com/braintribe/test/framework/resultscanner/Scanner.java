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
package com.braintribe.test.framework.resultscanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class Scanner {

	public Scanner() {
		
	}

	List<File> parseForPoms( File directory) {
		List<File> result = new ArrayList<File>();
		File [] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				result.addAll( parseForPoms(file));
			}
			else {
				if (file.getName().endsWith( ".pom")) {
					result.add( file);
				}
			}			
		}
		return result;
	}
	
	List<File> parseForDependency( List<File> poms, Artifact artifact) {
		List<File> result = new ArrayList<File>();
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = VersionProcessor.toString( artifact.getVersion());
		
		for (File pom : poms) {
			Document doc;
			try {
				doc = DomParser.load().from(pom);
			} catch (DomParserException e) {
				System.err.println();
				continue;
			}
			NodeList nodes = doc.getDocumentElement().getElementsByTagName("dependency");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				if (
						groupId.equalsIgnoreCase( DomUtils.getElementValueByPath(element, "groupId", false)) &&
						artifactId.equalsIgnoreCase( DomUtils.getElementValueByPath(element, "artifactId", false)) &&
						version.equalsIgnoreCase( DomUtils.getElementValueByPath(element, "version", false))
					) {
					result.add(pom);
				}
			}
		}
		return result;		
	}
	
	List<File> parseForParent( List<File> poms, Artifact artifact) {
		List<File> result = new ArrayList<File>();
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = VersionProcessor.toString( artifact.getVersion());
		
		for (File pom : poms) {
			Document doc;
			try {
				doc = DomParser.load().from(pom);
			} catch (DomParserException e) {
				System.err.println();
				continue;
			}
			NodeList nodes = doc.getDocumentElement().getElementsByTagName("parent");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				if (
						groupId.equalsIgnoreCase( DomUtils.getElementValueByPath(element, "groupId", false)) &&
						artifactId.equalsIgnoreCase( DomUtils.getElementValueByPath(element, "artifactId", false)) &&
						version.equalsIgnoreCase( DomUtils.getElementValueByPath(element, "version", false))
					) {
					result.add(pom);
				}
			}
		}
		return result;		
	}
	
	public static void main( String [] args) {		
		File root = new File( System.getenv( "M2_REPO"));
		Scanner scanner = new Scanner();
		List<File> poms = scanner.parseForPoms(root);
		System.out.println("\nFound [" + poms.size() + "] poms");
		
		for (String arg : args) {
			if (arg.equalsIgnoreCase( "-"))
				break;
			if (arg.equalsIgnoreCase(";")) {
				continue;
			}
			System.out.println(arg);
			Artifact artifact;
			try {
				artifact = NameParser.parseCondensedArtifact(arg);				
			} catch (NameParserException e) {
				throw new IllegalArgumentException("cannot parse [" + arg + "]");
			}
			
			List<File> result = scanner.parseForDependency(poms, artifact);
			System.out.println("Found [" + result.size() + "] matches on dependency");
			for (File file : result) {
				System.out.println( file.getAbsolutePath());
			}			
			result = scanner.parseForParent(poms, artifact);
			System.out.println("Found [" + result.size() + "] matches on parent");
			for (File file : result) {
				System.out.println( file.getAbsolutePath());
			}			
			
		}
	}
}
