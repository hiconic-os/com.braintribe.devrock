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
package com.braintribe.artifact.declared.marshaller.experts;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.artifact.declared.marshaller.PomReadContext;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

public class ParentExpert extends AbstractPomExpert implements HasPomTokens {

	public static VersionedArtifactIdentification read(PomReadContext context, XMLStreamReader reader) throws XMLStreamException  {
		
		VersionedArtifactIdentification dependency = create( context, VersionedArtifactIdentification.T);
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case GROUPID : {
							dependency.setGroupId( extractString(context, reader));
							break;
						}
						case ARTIFACTID: {
							dependency.setArtifactId( extractString(context, reader));
							break;
						}
						case VERSION: {
							dependency.setVersion( extractString(context, reader));							
							break;
						}
						default:
							skip(reader);
							break;
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return dependency;
				}
				default:
					break;
			}
			reader.next();
		}
		return null;
	}
}
