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
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.version.Version;

public class VersioningExpert extends AbstractMavenMetaDataExpert implements HasTokens{

	public static Versioning extract( MavenMetadataReadContext context, XMLStreamReader reader) throws XMLStreamException {
		
		Versioning versioning = create( context, Versioning.T);
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case tag_latest : {
							String versionAsString = extractString( context, reader);
							versioning.setLatest( Version.parse(versionAsString));
							break;
						}
						case tag_release: {
							String versionAsString = extractString( context, reader);
							versioning.setRelease( Version.parse(versionAsString));
							break;
						}
						case tag_lastUpdated: {
							String dateAsString = extractString( context, reader);
							versioning.setLastUpdated( dateAsString);
							/*
							versioning.setLastUpdated(timeFormat.parseDateTime(dateAsString).toDate());
							*/
							break;
						}
						case tag_versions : {
							versioning.setVersions(VersionsExpert.extract( context, reader));
							break;
						}
						case tag_snapshot : {
							versioning.setSnapshot( SnapshotExpert.extract( context, reader));
							break;
						}
						case tag_snapshotVersions : {
							versioning.setSnapshotVersions( SnapshotVersionsExpert.extract( context, reader));
							break;
						}
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return versioning;
				}
				default:
					break;
			}
			reader.next();
		}
		return versioning;
	}
	
	public static void write(XMLStreamWriter writer, Versioning value) throws XMLStreamException {
		if (value == null)
			return;
		writer.writeStartElement(tag_versioning);
		Version latest = value.getLatest();
		if (latest != null)
			write( writer, tag_latest, latest.asString());
		Version release = value.getRelease();
		if (release != null) 
			write( writer, tag_release, release.asString());
		/*
		write( writer, tag_lastUpdated, timeFormat.print( new DateTime(value.getLastUpdated().getTime())));
		*/
		write( writer, tag_lastUpdated, value.getLastUpdated());
		VersionsExpert.write(writer, value.getVersions());
		SnapshotExpert.write(writer, value.getSnapshot());
		SnapshotVersionsExpert.write( writer, value.getSnapshotVersions());
		writer.writeEndElement();
	}
}
