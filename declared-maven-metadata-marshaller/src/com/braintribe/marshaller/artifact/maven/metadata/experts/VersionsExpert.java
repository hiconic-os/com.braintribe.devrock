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

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.braintribe.marshaller.artifact.maven.metadata.MavenMetadataReadContext;
import com.braintribe.marshaller.artifact.maven.metadata.commons.HasTokens;
import com.braintribe.model.version.Version;

public class VersionsExpert extends AbstractMavenMetaDataExpert implements HasTokens {

	public static List<Version> extract( MavenMetadataReadContext context, XMLStreamReader reader) throws XMLStreamException {
		List<Version> versions = new ArrayList<Version>();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case tag_version : {
							String versionAsString = extractString( context, reader);
							versions.add( Version.parse( versionAsString));
							break;
						}
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return versions;
				}
				default:
					break;
			}
			reader.next();
		}
		return versions;
	}
	
	public static void write(XMLStreamWriter writer, List<Version> value) throws XMLStreamException {
		if (value != null && value.size() > 0) {
			writer.writeStartElement( tag_versions);
			for (Version version : value) {
				write( writer, tag_version, version.asString());
			}
			writer.writeEndElement();
		}
	}
}
