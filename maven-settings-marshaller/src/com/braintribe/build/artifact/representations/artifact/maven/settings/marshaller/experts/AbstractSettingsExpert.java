// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings.marshaller.experts;

import java.util.Stack;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

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
		StringBuilder builder = context.stringBuilder();
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
	
	
}
