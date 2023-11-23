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

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.model.artifact.maven.settings.Profile;

public class ActiveProfilesExpert extends AbstractSettingsExpert {
	public static List<Profile> read(SettingsMarshallerContext context, List<Profile> profiles, XMLStreamReader reader) throws XMLStreamException {
		List<Profile> result = new ArrayList<>();
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ACTIVE_PROFILE:
							String name = extractString(context, reader);
							boolean found = false;
							for (Profile profile : profiles) {
								if (name.equalsIgnoreCase(profile.getId())) {
									result.add( profile);
									found = true;
									break;
								}
							}
							if (!found) {
								throw new XMLStreamException("profile [" + name + "] is referenced as active profile, yet not declared amongst the profiles");
							}
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
