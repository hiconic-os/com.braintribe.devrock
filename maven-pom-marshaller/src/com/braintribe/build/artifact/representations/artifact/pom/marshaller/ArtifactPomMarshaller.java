// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.marshaller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts.HasPomTokens;
import com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts.PomReadContext;
import com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts.ProjectExpert;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Solution;

/**
 * an {@link XMLStreamReader} based POM marshaller (actually, currently only a reader) which returns a RAW solution, 
 * i.e. now smart resolving takes place (parent dependency, not resolved parent solution; raw properties, no resolved properties etc)
 * 
 * @author pit
 *
 */
public class ArtifactPomMarshaller implements HasPomTokens {
	private static Logger log = Logger.getLogger(ArtifactPomMarshaller.class);
	private static XMLInputFactory inputFactory;
	
	static {
		inputFactory = XMLInputFactory.newInstance();

		boolean debug = log.isDebugEnabled();
		try {
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
		} catch(Exception e) {
			if (debug) log.debug("Could not set feature "+XMLInputFactory.SUPPORT_DTD+"=false", e);
		}

		try {
			inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
		} catch(Exception e) {
			if (debug) log.debug("Could not set feature javax.xml.stream.isSupportingExternalEntities=false", e);
		}
		
		
	}
	/**
	 * unmarshall the pom from a reader
	 * @param reader - the {@link XMLStreamReader
	 * @return - the {@link Solution}
	 * @throws XMLStreamException
	 */
	public Solution unmarshall( XMLStreamReader reader) throws XMLStreamException {
		Solution solution = null;
		StringBuilder commonBuilder = new StringBuilder();
		PomReadContext context = () -> commonBuilder;
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
			case XMLStreamConstants.START_ELEMENT : 
				String tag = reader.getName().getLocalPart();{
					if (tag.equalsIgnoreCase( PROJECT )) {
						solution = ProjectExpert.read(context, reader);
					}
					break;
				}
			case XMLStreamConstants.END_ELEMENT : {
				return solution;
			}
			case XMLStreamConstants.END_DOCUMENT: {
				return solution;
			}
			default: 
				break;
			}
			reader.next();
		}	
		return solution;
	}

	/**
	 * unmarshall the file 
	 * @param file
	 * @return
	 * @throws XMLStreamException
	 */
	public Solution unmarshall( File file) throws XMLStreamException {	
		try (InputStream in = new BufferedInputStream(new FileInputStream( file))) {
			return unmarshall(in);
		} catch (Exception e) {
			throw new XMLStreamException(e);
		} 
	}

	/**
	 * unmarshall the input stream 
	 * @param in
	 * @return
	 * @throws XMLStreamException
	 */
	public Solution unmarshall(InputStream in) throws XMLStreamException {
		XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
		return unmarshall(reader);
	}
	
	/**
	 * unmarshall the POM as defined in the String passed  
	 * @param in
	 * @return
	 * @throws XMLStreamException
	 */
	public Solution unmarshall( String in) throws XMLStreamException {
		try (InputStream stream = new ByteArrayInputStream(in.getBytes(StandardCharsets.UTF_8));) {
			return unmarshall(stream);
		} catch (IOException e) {
			throw new XMLStreamException( e);
		}
	}
	
	
}
