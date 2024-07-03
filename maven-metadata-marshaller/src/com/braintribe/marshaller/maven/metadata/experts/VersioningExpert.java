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
