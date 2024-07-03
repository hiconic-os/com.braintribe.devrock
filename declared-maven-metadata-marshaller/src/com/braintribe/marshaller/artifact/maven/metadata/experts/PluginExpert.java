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
import com.braintribe.model.artifact.maven.meta.Plugin;

public class PluginExpert extends AbstractMavenMetaDataExpert implements HasTokens {

	public static Plugin extract( MavenMetadataReadContext context, XMLStreamReader reader) throws XMLStreamException {
		Plugin plugin = create( context, Plugin.T);
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						
						case tag_name: {
							plugin.setName( extractString( context, reader));
							break;
						}
						case tag_prefix : {
							plugin.setPrefix( extractString( context, reader));
							break;
						}
						case tag_artifactId : {
							plugin.setArtifactId(extractString( context, reader));
							break;
						}
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return plugin;
				}
				default:
					break;
			}
			reader.next();
		}
		return plugin;
	}
	
	public static void write(XMLStreamWriter writer, Plugin value) throws XMLStreamException {
		writer.writeStartElement( tag_plugin);
		write( writer, tag_name, value.getName());
		write( writer, tag_prefix, value.getPrefix());
		write( writer, tag_artifactId, value.getArtifactId());
		writer.writeEndElement();
	}
}
