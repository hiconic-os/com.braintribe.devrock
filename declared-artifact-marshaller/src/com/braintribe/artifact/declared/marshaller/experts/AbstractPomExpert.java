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
package com.braintribe.artifact.declared.marshaller.experts;


import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.braintribe.artifact.declared.marshaller.PomReadContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;

/**
 * an abstract common base class for all experts 
 * @author pit
 *
 */
public abstract class AbstractPomExpert {
	protected static DateTimeFormatter timeFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
	protected static DateTimeFormatter altTimeFormat = DateTimeFormat.forPattern("yyyyMMdd.HHmmss");
	
	/**
	 * read the text content of a tag 
	 * @param reader
	 * @return
	 * @throws XMLStreamException
	 */
	protected static String extractString(PomReadContext context, XMLStreamReader reader) throws XMLStreamException {
		StringBuilder buffer = context.getCommonStringBuilder();
		
		if (buffer.length() > 0)
			buffer.setLength(0);
		
		while (reader.hasNext()) {
			
			int eventType = reader.getEventType();
			switch (eventType) {
				case XMLStreamConstants.END_ELEMENT : {
					String res = toTrimmedString(buffer);
					return res.length() > 0 ? res : null;
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
	
	private static String toTrimmedString(StringBuilder builder) {
		int s = 0;
		int e = builder.length() - 1;
		int l = builder.length();
		
		while (s < l && Character.isWhitespace(builder.charAt(s))) {
			s++;
		}
		
		while (e >= 0 && Character.isWhitespace(builder.charAt(e))) {
			e--;
		}
		
		if (e < 0)
			return "";
		
		return builder.substring(s, e + 1);
	}
	
	/**
	 * skip a tag - careful : this requires that the caller DOES call reader.next() before it actually starts processing,
	 * otherwise, this here will be called out of sequence and gobble-up any tags.. 
	 * @param reader
	 * @throws XMLStreamException
	 */
	protected static void skip( XMLStreamReader reader) throws XMLStreamException {
		//System.out.println("initial ->" + reader.getLocalName());
		int elementDepth = 1;
		
		reader.next();
		while (reader.hasNext()) {
			int eventType = reader.getEventType();
			switch (eventType) {
				case XMLStreamConstants.START_ELEMENT: {
					//System.out.println("sub tag " + reader.getLocalName());
					elementDepth++;
					break;
				}
			
				case XMLStreamConstants.END_ELEMENT : {
					if (--elementDepth == 0)
						return; 
				}
				default:
					break;
			}
			reader.next();
		}
	}
	
	/**
	 * write a tag's value (currently not used)
	 * @param writer
	 * @param tag
	 * @param value
	 * @throws XMLStreamException
	 */
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
	protected static <T extends GenericEntity> T create(PomReadContext context, EntityType<T> entityType){
		T entity;
		if (context.getSession() != null) {
			try {
				entity = (T) context.getSession().create(entityType);
			} catch (RuntimeException e) {
				String msg ="instance provider cannot provide new instance of type [" + entityType.getTypeSignature() + "]";				
				throw new IllegalStateException(msg, e);
			}
		} 
		else {
			entity = (T) entityType.create();
		}
//		for (com.braintribe.model.generic.reflection.Property property : entityType.getProperties()) {
//			property.setAbsenceInformation(entity, GMF.absenceInformation());
//		}		
		return entity;
	}
	
}
