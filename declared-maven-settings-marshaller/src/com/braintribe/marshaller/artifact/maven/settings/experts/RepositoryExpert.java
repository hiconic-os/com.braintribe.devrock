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
package com.braintribe.marshaller.artifact.maven.settings.experts;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.model.artifact.maven.settings.Repository;

public class RepositoryExpert extends AbstractSettingsExpert {
	
	public static Repository read(SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		Repository result = create( context, Repository.T);
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ID:
							result.setId( extractString(context, reader));
						break;
						case URL:
							result.setUrl( extractString(context, reader));
							break;
						case NAME:
							result.setName( extractString(context, reader));
							break;
						case RELEASES:
							result.setReleases( RepositoryPolicyExpert.read( context, reader));
							break;
						case SNAPSHOTS:
							result.setSnapshots( RepositoryPolicyExpert.read( context, reader));
							break;
						case LAYOUT:
							result.setLayout( extractString(context, reader));
							break;
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return result;
				}
				default:
					break;
			}
			reader.next();
		}
		return null;
	}
}
