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

import java.util.Date;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.joda.time.DateTime;

import com.braintribe.marshaller.maven.metadata.HasTokens;
import com.braintribe.model.artifact.meta.Snapshot;

public class SnapshotExpert extends AbstractMavenMetaDataExpert implements HasTokens {

	public static Snapshot extract( XMLStreamReader reader) throws XMLStreamException {
		Snapshot snapshot = Snapshot.T.create();
		snapshot.setBuildNumber(0);
		snapshot.setLocalCopy(false);
		
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case tag_timestamp : {
							String dateAsString = extractString(reader);
							Date date;
							try {
								date = timeFormat.parseDateTime( dateAsString).toDate();
							} catch (Exception e) {
								date = altTimeFormat.parseDateTime( dateAsString).toDate();
							}
							snapshot.setTimestamp( date);
							break;
						}
						case tag_buildNumber: {
							String buildNumberAsString = extractString(reader);
							snapshot.setBuildNumber( Integer.parseInt(buildNumberAsString));
							break;
						}
						case tag_localCopy : {
							String localCopyAsString = extractString(reader);
							snapshot.setLocalCopy( Boolean.parseBoolean(localCopyAsString));
							break;
						}
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return snapshot;
				}
				default:
					break;
			}
			reader.next();
		}
		return null;
	}
	
	public static void write(XMLStreamWriter writer, Snapshot value) throws XMLStreamException {
		if (value == null) {
			return;
		}
		writer.writeStartElement( tag_snapshot);
		write( writer, tag_timestamp, timeFormat.print( new DateTime(value.getTimestamp().getTime())));
		write( writer, tag_buildNumber, "" + value.getBuildNumber());
		write( writer, tag_localCopy, "" + value.getLocalCopy());
		writer.writeEndElement();
	}
}
