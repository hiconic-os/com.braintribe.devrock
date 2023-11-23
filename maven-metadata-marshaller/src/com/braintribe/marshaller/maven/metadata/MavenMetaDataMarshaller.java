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
package com.braintribe.marshaller.maven.metadata;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.braintribe.logging.Logger;
import com.braintribe.marshaller.maven.metadata.experts.MavenMetaDataExpert;
import com.braintribe.marshaller.maven.metadata.writer.IndentingXmlStreamWriter;
import com.braintribe.model.artifact.meta.MavenMetaData;

/**
 * a maven metadata marshaller that uses {@link XMLStreamReader} to read the 
 * @author pit
 *
 */
public class MavenMetaDataMarshaller extends AbstractMetaDataCodec implements HasTokens {
	private static Logger log = Logger.getLogger(MavenMetaDataMarshaller.class);
	
	private static XMLInputFactory inputFactory;
	private static XMLOutputFactory outputFactory;
	private boolean indenting = true;

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
		
		outputFactory = XMLOutputFactory.newInstance();
	}
	
	public void marshall(File file, MavenMetaData value) throws XMLStreamException {
		try (OutputStream out = new FileOutputStream( file)) {
			marshall( out, value);
		}
		catch (Exception e) {
			throw new XMLStreamException(e);
		}
	}
	
	public String marshall( MavenMetaData value) throws XMLStreamException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			marshall(out, value);
			return out.toString();
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}
	
	public void marshall( OutputStream out, MavenMetaData value) throws XMLStreamException {
		XMLStreamWriter writer;
		out = new BufferedOutputStream(out);
		writer = !indenting ? outputFactory.createXMLStreamWriter(out, "UTF-8") : new IndentingXmlStreamWriter(out, "UTF-8");
		marshall( writer, value);
	}
	
	public void marshall( XMLStreamWriter writer, MavenMetaData value) throws XMLStreamException {
		writer.writeStartDocument();
		MavenMetaDataExpert.write( writer, value);
		writer.writeEndDocument();
		writer.flush();
		writer.close();
	}
	
	
	
	public MavenMetaData unmarshall( XMLStreamReader reader) throws XMLStreamException {
		MavenMetaData metaData = null;
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
			case XMLStreamConstants.START_ELEMENT : 
				String tag = reader.getName().getLocalPart();{
					if (tag.equalsIgnoreCase( tag_metaData )) {
						metaData = MavenMetaDataExpert.read(reader);
					}
					break;
				}
			case XMLStreamConstants.END_ELEMENT : {
				return metaData;
			}
			case XMLStreamConstants.END_DOCUMENT: {
				return metaData;
			}
			default: 
				break;
			}
			reader.next();
		}	
		return metaData;
	}

	public MavenMetaData unmarshall( File file) throws XMLStreamException {	
		try (InputStream in = new BufferedInputStream(new FileInputStream( file))) {
			XMLStreamReader reader = inputFactory.createXMLStreamReader( in);
			return unmarshall(reader);
		} catch (Exception e) {
			throw new XMLStreamException(e);
		} 
	}
	
	public MavenMetaData unmarshall(InputStream in) throws XMLStreamException {
		XMLStreamReader reader = inputFactory.createXMLStreamReader( in);
		return unmarshall(reader);
	}
	
	public MavenMetaData unmarshall( String in) throws XMLStreamException {
		try (InputStream stream = new ByteArrayInputStream(in.getBytes(StandardCharsets.UTF_8));) {
			return unmarshall(stream);
		} catch (IOException e) {
			throw new XMLStreamException( e);
		}
	}


}
