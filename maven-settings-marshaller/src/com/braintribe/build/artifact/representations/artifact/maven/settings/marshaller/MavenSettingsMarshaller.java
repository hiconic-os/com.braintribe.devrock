// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings.marshaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.build.artifact.representations.artifact.maven.settings.marshaller.experts.HasSettingsTokens;
import com.braintribe.build.artifact.representations.artifact.maven.settings.marshaller.experts.SettingsExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.marshaller.experts.SettingsMarshallerContext;
import com.braintribe.logging.Logger;
import com.braintribe.model.maven.settings.Settings;


/**
 * an {@link XMLStreamReader} based POM marshaller (actually, currently only a reader) which returns a RAW solution, 
 * i.e. now smart resolving takes place (parent dependency, not resolved parent solution; raw properties, no resolved properties etc)
 * 
 * @author pit
 *
 */
public class MavenSettingsMarshaller implements HasSettingsTokens {
	private static Logger log = Logger.getLogger(MavenSettingsMarshaller.class);
	private static XMLInputFactory inputFactory;
	private StringBuilder builder = new StringBuilder();
	
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
	public Settings unmarshall( XMLStreamReader reader) throws XMLStreamException {
		Settings solution = null;
		SettingsMarshallerContext context = () -> builder;
		
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
			case XMLStreamConstants.START_ELEMENT : 
				String tag = reader.getName().getLocalPart();{
					if (tag.equalsIgnoreCase( SETTINGS )) {
						solution = SettingsExpert.read(context, reader);
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
	public Settings unmarshall( File file) throws XMLStreamException {	
		try (InputStream in = new FileInputStream( file)) {			
			return unmarshall( in);
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
	public Settings unmarshall(InputStream in) throws XMLStreamException {
		XMLStreamReader reader = inputFactory.createXMLStreamReader( in);
		return unmarshall(reader);
	}
	
	/**
	 * unmarshall the POM as defined in the String passed  
	 * @param in
	 * @return
	 * @throws XMLStreamException
	 */
	public Settings unmarshall( String in) throws XMLStreamException {				
		XMLStreamReader reader = inputFactory.createXMLStreamReader( new StringReader(in));
		return unmarshall( reader);
		
	}
	
	
}
