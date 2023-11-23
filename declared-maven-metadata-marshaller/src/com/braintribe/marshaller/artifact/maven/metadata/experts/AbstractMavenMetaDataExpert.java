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
package com.braintribe.marshaller.artifact.maven.metadata.experts;





import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.braintribe.marshaller.artifact.maven.metadata.MavenMetadataReadContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public abstract class AbstractMavenMetaDataExpert {
	protected static DateTimeFormatter timeFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
	protected static DateTimeFormatter altTimeFormat = DateTimeFormat.forPattern("yyyyMMdd.HHmmss");
	
	protected static String extractString(MavenMetadataReadContext context,  XMLStreamReader reader) throws XMLStreamException {
		StringBuilder buffer = context.getCommonStringBuilder();
		if (buffer.length() > 0)
			buffer.setLength(0);
		
		while (reader.hasNext()) {
			
			int eventType = reader.getEventType();
			switch (eventType) {
				case XMLStreamConstants.END_ELEMENT : {
					return buffer.toString();
				}
				
				case XMLStreamConstants.CHARACTERS : {
					buffer.append( reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
					break;
				}
			}
			reader.next();
		}
		return null;
	}
	
	protected static void write( XMLStreamWriter writer, String tag, String value) throws XMLStreamException {
		writer.writeStartElement( tag);
		writer.writeCharacters( value);
		writer.writeEndElement();
	}

	/**
	 * create an new instance of the type passed, if the {@link SchemedXmlDeserializationOptions} contain a session, this is used
	 * @param entityType - the {@link EntityType}
	 * @return - the instantiated {@link GmEntityType}
	 */
	protected static <T extends GenericEntity> T create(MavenMetadataReadContext context, EntityType<T> entityType) throws XMLStreamException{
		if (context.getSession() != null) {
			try {
				return (T) context.getSession().create(entityType);
			} catch (RuntimeException e) {
				String msg ="instance provider cannot provide new instance of type [" + entityType.getTypeSignature() + "]";				
				throw new XMLStreamException(msg, e);
			}
		} 
		else {
			return (T) entityType.create();
		}
	}
}
