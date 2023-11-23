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

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.braintribe.marshaller.artifact.maven.metadata.MavenMetadataReadContext;
import com.braintribe.marshaller.artifact.maven.metadata.commons.HasTokens;
import com.braintribe.model.artifact.maven.meta.SnapshotVersion;

public class SnapshotVersionsExpert extends AbstractMavenMetaDataExpert implements HasTokens {

	public static List<SnapshotVersion> extract( MavenMetadataReadContext context, XMLStreamReader reader) throws XMLStreamException {
		List<SnapshotVersion> versions = new ArrayList<SnapshotVersion>();
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
					case tag_snapshotVersion:
						versions.add( SnapshotVersionExpert.extract(context, reader));
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
	
	public static void write(XMLStreamWriter writer, List<SnapshotVersion> value) throws XMLStreamException {
		if (value != null && value.size() > 0) {
			writer.writeStartElement( tag_snapshotVersions);
			for (SnapshotVersion version : value) {
				SnapshotVersionExpert.write(writer, version);
			}
			writer.writeEndElement();
		}
	}
}
