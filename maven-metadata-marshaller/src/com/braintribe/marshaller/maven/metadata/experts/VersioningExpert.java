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

import org.joda.time.DateTime;

import com.braintribe.marshaller.maven.metadata.HasTokens;
import com.braintribe.model.artifact.meta.Versioning;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;

public class VersioningExpert extends AbstractMavenMetaDataExpert implements HasTokens{

	public static Versioning extract( XMLStreamReader reader) throws XMLStreamException {
		
		Versioning versioning = Versioning.T.create();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case tag_latest : {
							String versionAsString = extractString( reader);
							versioning.setLatest( VersionProcessor.createFromString(versionAsString));
							break;
						}
						case tag_release: {
							String versionAsString = extractString( reader);
							versioning.setRelease( VersionProcessor.createFromString(versionAsString));
							break;
						}
						case tag_lastUpdated: {
							String dateAsString = extractString( reader);
							versioning.setLastUpdated(timeFormat.parseDateTime(dateAsString).toDate());
							break;
						}
						case tag_versions : {
							versioning.setVersions(VersionsExpert.extract( reader));
							break;
						}
						case tag_snapshot : {
							versioning.setSnapshot( SnapshotExpert.extract( reader));
							break;
						}
						case tag_snapshotVersions : {
							versioning.setSnapshotVersions( SnapshotVersionsExpert.extract( reader));
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
		return null;
	}
	
	public static void write(XMLStreamWriter writer, Versioning value) throws XMLStreamException {
		if (value == null)
			return;
		writer.writeStartElement(tag_versioning);
		Version latest = value.getLatest();
		if (latest != null)
			write( writer, tag_latest, VersionProcessor.toString( latest));
		Version release = value.getRelease();
		if (release != null) 
			write( writer, tag_release, VersionProcessor.toString( release));
		write( writer, tag_lastUpdated, timeFormat.print( new DateTime(value.getLastUpdated().getTime())));
		VersionsExpert.write(writer, value.getVersions());
		SnapshotExpert.write(writer, value.getSnapshot());
		SnapshotVersionsExpert.write( writer, value.getSnapshotVersions());
		writer.writeEndElement();
	}
}
