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
