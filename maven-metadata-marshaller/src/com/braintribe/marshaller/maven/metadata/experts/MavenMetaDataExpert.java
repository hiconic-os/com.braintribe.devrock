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
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;

public class MavenMetaDataExpert extends AbstractMavenMetaDataExpert implements HasTokens {
	
	public static MavenMetaData read( XMLStreamReader reader) throws XMLStreamException {
		MavenMetaData mavenMetaData = MavenMetaData.T.create();
		
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case tag_groupId : {
							mavenMetaData.setGroupId( extractString( reader));
							break;
						}
						case tag_artifactId: {
							mavenMetaData.setArtifactId( extractString( reader));
							break;
						}
						case tag_version: {
							String versionAsString = extractString( reader);
							mavenMetaData.setVersion( VersionProcessor.createFromString(versionAsString));
							break;
						}
						case tag_versioning : {
							mavenMetaData.setVersioning( VersioningExpert.extract( reader));
							break;
						}
						case tag_plugins : {
							mavenMetaData.setPlugins( PluginsExpert.extract( reader));
							break;
						}
					}
					break;
				}
				case XMLStreamConstants.COMMENT : {									
					mavenMetaData.setMcComment( reader.getText());
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
		return null;
	}
	
	public static void write(XMLStreamWriter writer, MavenMetaData value) throws XMLStreamException {
		writer.writeStartElement( tag_metaData);
		if (value.getMcComment() != null) {
			writer.writeComment( value.getMcComment());
		}
		write(writer, tag_groupId, value.getGroupId());
		write( writer, tag_artifactId, value.getArtifactId());
		Version version = value.getVersion();
		if (version != null) {
			write( writer, tag_version, VersionProcessor.toString( version));
		}
		VersioningExpert.write(writer, value.getVersioning());
		PluginsExpert.write( writer, value.getPlugins());
		writer.writeEndElement();
		
	}
}
