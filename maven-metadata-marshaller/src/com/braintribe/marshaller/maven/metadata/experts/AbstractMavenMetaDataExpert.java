// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.marshaller.maven.metadata.experts;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public abstract class AbstractMavenMetaDataExpert {
	protected static DateTimeFormatter timeFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
	protected static DateTimeFormatter altTimeFormat = DateTimeFormat.forPattern("yyyyMMdd.HHmmss");
	
	protected static String extractString( XMLStreamReader reader) throws XMLStreamException {
		StringBuilder buffer = new StringBuilder();
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
	
}
