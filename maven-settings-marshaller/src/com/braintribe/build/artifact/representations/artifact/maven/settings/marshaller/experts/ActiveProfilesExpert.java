// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings.marshaller.experts;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.model.maven.settings.Profile;

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
