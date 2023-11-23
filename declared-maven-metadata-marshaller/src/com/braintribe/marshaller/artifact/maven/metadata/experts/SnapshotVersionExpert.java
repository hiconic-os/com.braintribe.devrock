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

import com.braintribe.marshaller.artifact.maven.metadata.MavenMetadataReadContext;
import com.braintribe.marshaller.artifact.maven.metadata.commons.HasTokens;
import com.braintribe.model.artifact.maven.meta.SnapshotVersion;

public class SnapshotVersionExpert extends AbstractMavenMetaDataExpert implements HasTokens {

	public static SnapshotVersion extract( MavenMetadataReadContext context, XMLStreamReader reader) throws XMLStreamException {
		SnapshotVersion version = create( context, SnapshotVersion.T);
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case tag_updated : {
							String dateAsString = extractString(context, reader);
							version.setUpdated(dateAsString);
							/*
							Date date;
							try {
								date = timeFormat.parseDateTime( dateAsString).toDate();
							} catch (Exception e) {
								date = altTimeFormat.parseDateTime( dateAsString).toDate();
							}
							version.setUpdated(date);
							*/
							break;
						}
						case tag_classifier: {
							version.setClassifier( extractString( context, reader));
							break;
						}
						case tag_extension : {
							version.setExtension( extractString( context, reader));
							break;
						}
						case tag_value : {
							version.setValue( extractString( context, reader));
							break;
						}
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return version;
				}
				default:
					break;
			}
			reader.next();
		}
		return version;
	}
	
	public static void write(XMLStreamWriter writer, SnapshotVersion value) throws XMLStreamException {
		writer.writeStartElement(tag_snapshotVersion);
		/*
		write( writer, tag_updated, timeFormat.print( new DateTime(value.getUpdated().getTime())));
		*/
		write( writer, tag_updated, value.getUpdated());
		String classifier = value.getClassifier();
		if (classifier != null) {
			write( writer, tag_classifier, classifier);
		}
		write( writer, tag_extension, value.getExtension());
		write( writer, tag_value, value.getValue());
		writer.writeEndElement();		
	}
}
