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
