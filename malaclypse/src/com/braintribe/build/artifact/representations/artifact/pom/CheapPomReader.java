// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Exclusion;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;
import com.braintribe.utils.xml.parser.builder.LoadDocumentContext;

/**
 * a cheap and simple reader for artifacts.<br/> 
 * it doesn't read any parents, nor dependencies, nor properties<br/>
 * <br/>
 * it cannot read any grouped artifacts (as for that it would need to resolve parents), but 
 * it can read our new standard poms as described here <br/>
 * https://docs.google.com/document/d/1URIdk96qvFiwZmFSRB5fMOihK4CoEJtmoFfawAvU-hs/edit#heading=h.jjkcnrc3kjyz
 * 
 * @author pit
 *
 */
public class CheapPomReader {

	private static final String TOKEN_CLASSIFIER = "classifier";
	private static final String TOKEN_PACKAGING = "packaging";
	private static final String TOKEN_VERSION = "version";
	private static final String TOKEN_ARTIFACT_ID = "artifactId";
	private static final String TOKEN_GROUP_ID = "groupId";
	
	private static final String TOKEN_PARENT = "parent";
	
	private static final String TOKEN_PROJECT = "project";
	private static final String TOKEN_DEPENDENCIES = "dependencies";
	private static final String TOKEN_DEPENDENCY = "dependency";
	private static final String TOKEN_SCOPE = "scope";
	private static final String TOKEN_TYPE = "type";
	
	private static final String TOKEN_EXCLUSIONS = "exclusions";
	private static final String TOKEN_EXCLUSION = "exclusion";
	
	private static final String TOKEN_PROPERTIES = "properties";

	private static LoadDocumentContext loadDocumentContext = DomParser.load();

	/**
	 * read the artifact declaration section of a pom and return the extracted information as an {@link Artifact}
	 * @param pom - the {@link File} that represents the pom
	 * @return - an {@link Artifact} with the pertinent data 
	 * @throws PomReaderException - arrgh
	 */
	public static Artifact identifyPom( File pom) throws PomReaderException{
		try {
			if (pom.exists()) {
				Document doc = loadDocumentContext.from(pom);
				return identifyPom(doc);
			} 
			else {
				String msg = "File [" + pom + "] doesn't exist";
				throw new PomReaderException(msg);
			}
		} catch (Exception e) {
			String msg = "cannot load pom file [" + pom.getAbsolutePath() + "]";
			throw new PomReaderException(msg, e); 			
		}
	}
	
	public static Artifact identifyPom( String contents) throws PomReaderException {
		try {
			Document doc = DomParser.load().from( contents);
			return identifyPom(doc);		
		} catch (Exception e) {
			String msg = "cannot load pom from string [" + contents + "]";
			throw new PomReaderException(msg, e); 			
		}
	}
	
	private static Artifact identifyPom( Document doc) throws PomReaderException {
		
		Element projectE = doc.getDocumentElement();
		Map<String,String> properties = primeProperties(projectE);
		
		String groupId = DomUtils.getElementValueByPath(projectE, TOKEN_GROUP_ID, false);
		if (groupId == null) {
			groupId = DomUtils.getElementValueByPath(projectE, TOKEN_PARENT + "/" + TOKEN_GROUP_ID, false);
			if (groupId == null) {
				String msg = String.format("cannot find groupid declaration in pom ");
				throw new PomReaderException(msg); 			
			}
		}
		groupId = resolve(properties, groupId);
		
		String artifactId = DomUtils.getElementValueByPath(projectE, TOKEN_ARTIFACT_ID, false);
		if (artifactId == null) {
			String msg = String.format("cannot find artifactid declaration in pom ");
			throw new PomReaderException(msg); 			
		}
		
		artifactId = resolve(properties, artifactId);
		
		String version = DomUtils.getElementValueByPath(projectE, TOKEN_VERSION, false);
		if (version == null) {
			String msg = String.format("cannot find version declaration in pom");
			throw new PomReaderException(msg); 			
		}
		
		version = resolve(properties, version);
		
		String packaging = DomUtils.getElementValueByPath( projectE, TOKEN_PACKAGING, false);
		String classifier = DomUtils.getElementValueByPath( projectE, TOKEN_CLASSIFIER, false);
		
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId(groupId);
		artifact.setArtifactId(artifactId);
		try {
			artifact.setVersion( VersionProcessor.createFromString(version));
		} catch (VersionProcessingException e) {
			String msg = String.format("cannot extract valid version from declaration [%s] in pom", version);
			throw new PomReaderException(msg); 	
		}
		if (packaging == null) {
			packaging = "jar";
		}
		artifact.setPackaging(packaging);
		if (classifier != null)
			artifact.setClassifier(classifier);
		
		return artifact;
	}
	
	/**
	 * cheap pom codec -> creates a document that reflects a MINIMAL pom (no parent, just artifact plus dependencies)
	 * @param artifact - the {@link Artifact} to reflect in a pom
	 * @return - a {@link Document} that contains the pom
	 * @throws PomReaderException - if anything goes wrong
	 */
	public static Document write( Artifact artifact) throws PomReaderException{
		Document document;
		try {
			document = DomParser.create().makeItSo();			
		} catch (DomParserException e) {
			throw new PomReaderException( e);
		}
		//
		Element projectE = document.createElement(TOKEN_PROJECT);
		document.appendChild(projectE);
		
		DomUtils.setElementValueByPath(projectE, TOKEN_ARTIFACT_ID, artifact.getArtifactId(), true);
		DomUtils.setElementValueByPath(projectE, TOKEN_GROUP_ID, artifact.getGroupId(), true);		
		DomUtils.setElementValueByPath(projectE, TOKEN_VERSION, VersionProcessor.toString( artifact.getVersion()), true);
		
		
		String classifier = artifact.getClassifier();
		if (classifier != null) {
			DomUtils.setElementValueByPath(projectE, TOKEN_CLASSIFIER, classifier, true);
		}
		String packaging = artifact.getPackaging();
		if (packaging != null) {
			DomUtils.setElementValueByPath(projectE, TOKEN_PACKAGING, packaging, true);
		}
		
		List<Dependency> dependencies = artifact.getDependencies();
		if (dependencies.size() > 0) {
			Element dependenciesE = document.createElement(TOKEN_DEPENDENCIES);
			projectE.appendChild(dependenciesE);
			for (Dependency dependency : dependencies) {
				Element dependencyE = document.createElement(TOKEN_DEPENDENCY);
				dependenciesE.appendChild(dependencyE);
				DomUtils.setElementValueByPath(dependencyE, TOKEN_GROUP_ID, dependency.getGroupId(), true);
				DomUtils.setElementValueByPath(dependencyE, TOKEN_ARTIFACT_ID, dependency.getArtifactId(), true);				
				DomUtils.setElementValueByPath(dependencyE, TOKEN_VERSION, VersionRangeProcessor.toString(dependency.getVersionRange()), true);				
				String dependencyClassifier = dependency.getClassifier();
				if (dependencyClassifier != null) {
					DomUtils.setElementValueByPath(dependencyE, TOKEN_CLASSIFIER, classifier, true);
				}
				String dependencyScope = dependency.getScope();
				if (dependencyScope != null) {
					DomUtils.setElementValueByPath(dependencyE, TOKEN_SCOPE, dependencyScope, true);
				}
				
				String dependencyType = dependency.getType();
				if (dependencyType != null) {
					DomUtils.setElementValueByPath(dependencyE, TOKEN_TYPE, dependencyType, true);
				}
				
				Set<Exclusion> exclusions = dependency.getExclusions();
				if (exclusions.size() > 0) {
					Element exclusionsE = document.createElement( TOKEN_EXCLUSIONS);
					dependenciesE.appendChild(exclusionsE);
					
					for (Exclusion exclusion : exclusions) {
						Element exclusionE = document.createElement(TOKEN_EXCLUSION);
						exclusionsE.appendChild(exclusionE);
						DomUtils.setElementValueByPath(exclusionE, TOKEN_GROUP_ID, exclusion.getGroupId(), true);
						DomUtils.setElementValueByPath(exclusionE, TOKEN_ARTIFACT_ID, exclusion.getArtifactId(), true);
					}
				}
			}
		}
		
		
		return document;
	}
	
	private static Map<String,String> primeProperties(Element documentElement) {
		Map<String, String> properties = new HashMap<String, String>();

		Element propertiesE = DomUtils.getElementByPath( documentElement, TOKEN_PROPERTIES, false);
		if (propertiesE == null) {
			return properties;
		}
		NodeList nodes = propertiesE.getChildNodes();
		for (int i = 0 ; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				Element nodeE = (Element) node;
				properties.put( nodeE.getTagName(), nodeE.getTextContent());
			}		
		}
		return properties;
	}
		
	private static String resolve(Map<String, String> properties, String value) {
		
		while (requiresEvaluation( value)) {
			String variable = extract( value);
			String resolvedValue = properties.get(variable);
			String newValue = replace( variable, resolvedValue, value);
			if (newValue.equalsIgnoreCase(value)) {
				return value;
			}
			else {
				value = newValue;
			}			
		}
		return value;		
	}
	private static boolean requiresEvaluation(String expression) {
		String extract = extract(expression);
		return !extract.equalsIgnoreCase(expression);
	}

	/**
	 * extracts the first variable in the expression 
	 * @param expression - the {@link String} to extract the variable from 
	 * @return - the first variable (minus the ${..} stuff)
	 */
	private static String extract(String expression) {
		int p = expression.indexOf( "${");
		if (p < 0)
			return expression;
		int q = expression.indexOf( "}", p+1);
		return expression.substring(p+2, q);	
	}

	/**
	 * replaces any occurrence of the variable by its value 
	 * @param variable - without ${..}, it will be added 
	 * @param value - the value of the variable
	 * @param expression - the expression to replace in 
	 * @return - the expression after the replace 
	 */
	private static String replace(String variable, String value, String expression) {
		return expression.replace( "${" + variable + "}", value);
	}
		
}
