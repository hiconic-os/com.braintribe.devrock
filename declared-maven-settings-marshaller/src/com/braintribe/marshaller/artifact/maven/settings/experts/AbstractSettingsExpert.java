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
package com.braintribe.marshaller.artifact.maven.settings.experts;

import java.util.Stack;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;


/**
 * an abstract common base class for all experts 
 * @author pit
 *
 */
public abstract class AbstractSettingsExpert implements HasSettingsTokens  {
/**
	 * read the text content of a tag 
	 * @param reader
	 * @return
	 * @throws XMLStreamException
	 */
	protected static String extractString( SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		StringBuilder builder = context.getCommonStringBuilder();
		builder.setLength(0);
		while (reader.hasNext()) {
			
			int eventType = reader.getEventType();
			switch (eventType) {
				case XMLStreamConstants.END_ELEMENT : {
					return builder.toString();
				}
				
				case XMLStreamConstants.CHARACTERS : {
					builder.append( reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
					break;
				}
			}
			reader.next();
		}
		return null;
	}
	/**
	 * skip a tag - careful : this requires that the caller DOES call reader.next() before it actually starts processing,
	 * otherwise, this here will be called out of sequence and gobble-up any tags.. 
	 * @param reader
	 * @throws XMLStreamException
	 */
	protected static void skip( XMLStreamReader reader) throws XMLStreamException {
		//System.out.println("initial ->" + reader.getLocalName());
		Stack<String> stack = new Stack<>();
		stack.push(reader.getLocalName());
		
		reader.next();
		while (reader.hasNext()) {
			int eventType = reader.getEventType();
			switch (eventType) {
				case XMLStreamConstants.START_ELEMENT: {
					//System.out.println("sub tag " + reader.getLocalName());
					stack.push(reader.getLocalName());
					break;
				}
			
				case XMLStreamConstants.END_ELEMENT : {
					stack.pop();
					if (stack.isEmpty())
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
	 * 
	 * @param entityType - the {@link EntityType}
	 * @return - the instantiated {@link GenericEntity}
	 */
	protected static <T extends GenericEntity> T create(SettingsMarshallerContext context, EntityType<T> entityType) throws XMLStreamException{
		T entity;
		if (context.getSession() != null) {
			try {
				entity = (T) context.getSession().create(entityType);
			} catch (RuntimeException e) {
				String msg ="instance provider cannot provide new instance of type [" + entityType.getTypeSignature() + "]";				
				throw new XMLStreamException(msg, e);
			}
		} 
		else {
			entity = (T) entityType.create();
		}
		for (com.braintribe.model.generic.reflection.Property property : entityType.getProperties()) {
			property.setAbsenceInformation(entity, GMF.absenceInformation());
		}		
		return entity;
	}
	
}
