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
