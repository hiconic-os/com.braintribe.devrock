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
