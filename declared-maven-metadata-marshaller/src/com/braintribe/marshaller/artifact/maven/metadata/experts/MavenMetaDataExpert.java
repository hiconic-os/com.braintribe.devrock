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
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.version.Version;

public class MavenMetaDataExpert extends AbstractMavenMetaDataExpert implements HasTokens {
	
	public static MavenMetaData read( MavenMetadataReadContext context, XMLStreamReader reader) throws XMLStreamException {
		MavenMetaData mavenMetaData = create( context, MavenMetaData.T);
		
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case tag_groupId : {
							mavenMetaData.setGroupId( extractString( context, reader));
							break;
						}
						case tag_artifactId: {
							mavenMetaData.setArtifactId( extractString( context, reader));
							break;
						}
						case tag_version: {
							String versionAsString = extractString( context, reader);
							mavenMetaData.setVersion( Version.parse(versionAsString));
							break;
						}
						case tag_versioning : {
							mavenMetaData.setVersioning( VersioningExpert.extract( context, reader));
							break;
						}
						case tag_plugins : {
							mavenMetaData.setPlugins( PluginsExpert.extract( context, reader));
							break;
						}
					}
					break;
				}				
				case XMLStreamConstants.END_ELEMENT : {
					return mavenMetaData;
				}
				default:
					break;
			}
			reader.next();
		}
		return mavenMetaData;
	}
	
	public static void write(XMLStreamWriter writer, MavenMetaData value) throws XMLStreamException {
		writer.writeStartElement( tag_metaData);
		write(writer, tag_groupId, value.getGroupId());
		write( writer, tag_artifactId, value.getArtifactId());
		Version version = value.getVersion();
		if (version != null) {
			write( writer, tag_version, version.asString());
		}
		VersioningExpert.write(writer, value.getVersioning());
		PluginsExpert.write( writer, value.getPlugins());
		writer.writeEndElement();
		
	}
}
