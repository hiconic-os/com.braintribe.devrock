package com.braintribe.build.artifact.test.repolet.generator.preprocessor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * renames the groups in a specific file system, touching poms and maven-metadata
 * 
 * @author pit
 *
 */
public class PreProcessor {
	private boolean dryRun = false;
	private boolean backup = true;

	public void transpose( File root, String ogrpId, String grpId) {
		
		//
		
		List<File> filesToTouch = findRelevantFiles(root);
		filesToTouch.stream().forEach( f -> {
			if (f.getName().endsWith( ".pom")) {
				transposePom( f, ogrpId, grpId);
			}
			else {			
				transposeMetadata(f, grpId);			
			}
		});
		
	}
	
	private void transposeMetadata(File f, String grpId) {
		Document metaDoc;
		
		metaDoc = loadFile(f);		
		
		if (backup && !dryRun) {
			backup(f, metaDoc);
		}
	
		Element parent = metaDoc.getDocumentElement();
		DomUtils.setElementValueByPath(parent, "groupId", grpId, false);
	
		writeFile(f, metaDoc);
	}

	private void writeFile(File f, Document metaDoc) {
		if (!dryRun) {
			try {
				DomParser.write().from(metaDoc).to(f);
			} catch (DomParserException e) {
				throw new IllegalStateException("cannot write to [" + f.getAbsolutePath() + "]", e);
			}
		}
		else {
			System.out.println("--------------------------------------");
			System.out.println( f.getAbsolutePath());
			try {
				System.out.println(DomParser.write().from(metaDoc).to());
			} catch (DomParserException e) {
				throw new IllegalStateException("cannot write contents", e);
			}
			System.out.println("--------------------------------------");
		}
	}

	private void backup(File f, Document metaDoc) {
		File o = new File( f.getParentFile(), f.getName()+ ".bak");
		try {
			DomParser.write().from(metaDoc).to( o);
		} catch (DomParserException e) {
			throw new IllegalStateException("cannot write [" + o.getAbsolutePath() + "]", e);
		}
	}

	private Document loadFile(File f) {
		try {
			return DomParser.load().from(f);
		} catch (DomParserException e) {
			throw new IllegalStateException("cannot read [" + f.getAbsolutePath() + "]", e);
		}
		
	}

	private void transposePom(File f, String ogrpId, String grpId) {
		Document pom = loadFile(f);
		
		if (backup && !dryRun) {
			backup(f, pom);
		}
	
		Element parent = pom.getDocumentElement();
		DomUtils.setElementValueByPath(parent, "groupId", grpId, false);
		//DomUtils.setElementValueByPath(parent, "artifacId", artId, false);
		
		DomUtils.setElementValueByPath(parent, "parent/groupId", grpId, false);
		//DomUtils.setElementValueByPath(parent, "parent/artifacId", artId, false);
		
		// dependencies
		Element dependencies = DomUtils.getElementByPath(parent, "dependencies", false);
		if (dependencies != null) {
			transposeDependencies( dependencies, ogrpId, grpId);
		}
		// properties
		Element properties = DomUtils.getElementByPath(parent, "properties", false);
		if (properties != null) {
			transposeProperties( properties, ogrpId, grpId);
		}
		
		// dependency management : parent
		Element mgmt = DomUtils.getElementByPath(parent, "dependencyManagement", false);
		if (mgmt != null) {
			transposeMngmt( mgmt, ogrpId, grpId);
		}
		writeFile(f, pom);
		
	}

	private void transposeMngmt(Element mgmt, String ogrpId, String grpId) {
		Iterator<Element> elementIterator = DomUtils.getElementIterator(mgmt, "dependency");
		while (elementIterator.hasNext()) {
			Element dependency = elementIterator.next();
			
			String grp = DomUtils.getElementValueByPath(dependency, "groupId", false);			
			if (
					grp != null && grp.equalsIgnoreCase( ogrpId)
				) {
				DomUtils.setElementValueByPath(dependency, "groupId", grpId, false);
				
			}
			
		}
		
	}

	private void transposeProperties(Element properties, String ogrpId, String grpId) {
		Iterator<Element> elementIterator = DomUtils.getElementIterator(properties, ".*");
		Set<Element> elementsToDrop = new HashSet<>();
		while (elementIterator.hasNext()) {
			Element property = elementIterator.next();
			String tagName = property.getTagName();
			if (tagName.equalsIgnoreCase( "V."+ ogrpId)) {
				String value = property.getTextContent();
				Element newProperty = properties.getOwnerDocument().createElement( "V."+grpId);
				newProperty.setTextContent( value);
				properties.getOwnerDocument().adoptNode(newProperty);
				properties.appendChild(newProperty);
				elementsToDrop.add(property);
			}
			elementsToDrop.stream().forEach( e -> {
				properties.removeChild( e);				
			});
		}
		
	}

	private void transposeDependencies(Element dependencies, String ogrpId, String grpId) {
		Iterator<Element> elementIterator = DomUtils.getElementIterator(dependencies, "dependency");
		while (elementIterator.hasNext()) {
			Element dependency = elementIterator.next();
			
			String grp = DomUtils.getElementValueByPath(dependency, "groupId", false);
			
			if (grp != null && grp.equalsIgnoreCase( ogrpId)) {
				DomUtils.setElementValueByPath(dependency, "groupId", grpId, false);
				String version = DomUtils.getElementValueByPath(dependency, "version", false);
				if (version.equalsIgnoreCase("${V." + ogrpId + "}")) {
					DomUtils.setElementValueByPath(dependency, "version", "${V." + grpId + "}", false);
				}
			}
		}
								
	}

	private List<File> findRelevantFiles(File envelope) {
		List<File> result = new ArrayList<File>();
		File [] files = envelope.listFiles();
		if (files == null)
			return Collections.emptyList();
		for (File file : files) {
			if (file.isDirectory()) {
				result.addAll( findRelevantFiles(file));
			}
			String name = file.getName();
			if (name.endsWith( ".pom")) {
				result.add(file);
			}
			else if (name.startsWith( "maven-metadata") && name.endsWith( ".xml")) {
				result.add( file);
			}			
		}
		return result;
	}
	
	public static void main( String [] args) {
		File root = new File( args[0]);
		String ogrpId = args[1];
		String grpId = args[2];

		PreProcessor preProc = new PreProcessor();
		preProc.transpose(root, ogrpId, grpId);
	}
}
