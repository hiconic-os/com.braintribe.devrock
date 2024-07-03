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

import com.braintribe.marshaller.maven.metadata.HasTokens;
import com.braintribe.model.artifact.meta.Plugin;

public class PluginExpert extends AbstractMavenMetaDataExpert implements HasTokens {

	public static Plugin extract( XMLStreamReader reader) throws XMLStreamException {
		Plugin plugin = Plugin.T.create();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						
						case tag_name: {
							plugin.setName( extractString(reader));
							break;
						}
						case tag_prefix : {
							plugin.setPrefix( extractString(reader));
							break;
						}
						case tag_artifactId : {
							plugin.setArtifactId(extractString(reader));
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
		return null;
	}
	
	public static void write(XMLStreamWriter writer, Plugin value) throws XMLStreamException {
		writer.writeStartElement( tag_plugin);
		write( writer, tag_name, value.getName());
		write( writer, tag_prefix, value.getPrefix());
		write( writer, tag_artifactId, value.getArtifactId());
		writer.writeEndElement();
	}
}
