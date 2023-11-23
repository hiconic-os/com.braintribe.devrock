// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.quickscan.agnostic;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.braintribe.build.quickscan.QuickImportScanException;
import com.braintribe.build.quickscan.commons.QuickImportScannerCommons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.panther.ProjectNature;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;
import com.braintribe.utils.xml.parser.builder.LoadDocumentContext;

public class ScanTuple {
	private static final String NATURES = "$(natures}";
	private static Logger log = Logger.getLogger(ScanTuple.class);
	private File project;
	private File pom;
	private File ant;
	private SourceArtifact sourceArtifact;
	private Document document;
	private String scanKey;
	private Map<String, String> properties;
	private Version resolvedVersion;
	private VersionRange parentVersionRange;
	private static LoadDocumentContext loadingContext = DomParser.load();
	
	public ScanTuple(File project, File pom, File ant) {
		this.project = project;
		this.pom = pom;
		this.ant = ant;
	}

	public File getProject() {
		return project;
	}

	public File getPom() {
		return pom;
	}
	
	public File getAnt() {
		return ant;
	}

	public SourceArtifact getSourceArtifact() {
		return sourceArtifact;
	}

	public void setSourceArtifact(SourceArtifact sourceArtifact) {
		this.sourceArtifact = sourceArtifact;
	}

	public String getScanKey() {
		return scanKey;
	}

	public void setScanKey(String scanKey) {
		this.scanKey = scanKey;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
	
	public Version getResolvedVersion() {
		return resolvedVersion;
	}

	public void setResolvedVersion(Version resolvedVersion) {
		this.resolvedVersion = resolvedVersion;
	}

	public VersionRange getParentVersionRange() {
		return parentVersionRange;
	}

	public void setParentVersionRange(VersionRange parentVersionRange) {
		this.parentVersionRange = parentVersionRange;
	}

	public void prime(SourceRepository sourceRepository) throws QuickImportScanException {
		if (document == null) {
			try {
				document = loadingContext.from( pom);
			} catch (DomParserException e) {
				throw new QuickImportScanException("cannot read [" + pom.getAbsolutePath() + "]", e);
			}
		}
		primeProperties();

		sourceArtifact = SourceArtifact.T.create(); 			 		
		String groupIdAsSet = DomUtils.getElementValueByPath( document.getDocumentElement(), "groupId", false);
		if (groupIdAsSet == null) {
			if (groupIdAsSet == null) {
				groupIdAsSet = resolve(DomUtils.getElementValueByPath( document.getDocumentElement(), "parent/groupId", false));
			}
		}
		
		String groupId = resolve(groupIdAsSet);
		if (groupId != null) {
			sourceArtifact.setGroupId( groupId);		
		}
		else {
			sourceArtifact.setGroupId( groupIdAsSet);
		}
		
		
		String versionAsSet = DomUtils.getElementValueByPath( document.getDocumentElement(), "version", false);
		String version = resolve(versionAsSet);
		if (version != null) {
			sourceArtifact.setVersion(version);
		}
		else {
			sourceArtifact.setVersion( versionAsSet);	
		}

		// this is the only one that must be there 
		sourceArtifact.setArtifactId( resolve(DomUtils.getElementValueByPath( document.getDocumentElement(), "artifactId", false)));
		sourceArtifact.setPath( QuickImportScannerCommons.derivePath(pom.getParentFile(), sourceRepository));
		sourceArtifact.setRepository(sourceRepository);
		
		String naturesAsString = properties.get( NATURES);
		if (naturesAsString != null && naturesAsString.length() > 0) {
			String [] natures = naturesAsString.split( ",");
			for (String nature : natures) {
				try {
					ProjectNature projectNature = ProjectNature.valueOf(nature);
					sourceArtifact.getNatures().add(projectNature);
				} catch (Exception e) {					
					log.error( "property [natures] contains an unknown value [" + nature + "] in [" + pom.getAbsolutePath() + "]");
				}				
			}
		}
		else {
			// eclipse and ant are default
			sourceArtifact.getNatures().add( ProjectNature.ant);
			sourceArtifact.getNatures().add( ProjectNature.eclipse);
		}
		
	}

	public String resolve(String value) {
		if (value == null)
			return null;
		String expression = value;
		while (requiresEvaluation(expression)) {			
			String variable = extract(expression);
			String resolved = properties.get( variable);
			if (resolved != null) {
				expression = expression.replace( variable, resolved);
			}			
			else {
				//throw new QuickImportScanException("cannot resolve [" + variable + "] in expression [" + value + "]");
				return null;
			}
		}
		return expression;
		
	}
	private boolean requiresEvaluation(String expression) {
		return expression.contains( "${");
	}
	
	protected String extract(String expression) {
		int p = expression.indexOf( "${");
		if (p < 0)
			return expression;
		int q = expression.indexOf( "}", p+1);
		return expression.substring(p, q+1);	
	}
	
	private void primeProperties() {
		properties = new HashMap<String, String>();

		Element propertiesE = DomUtils.getElementByPath( document.getDocumentElement(), "properties", false);
		if (propertiesE == null) {
			return;
		}
		NodeList nodes = propertiesE.getChildNodes();
		for (int i = 0 ; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				Element nodeE = (Element) node;
				properties.put( "${" + nodeE.getTagName() + "}", nodeE.getTextContent());
			}		
		}
	}
	
	

}
