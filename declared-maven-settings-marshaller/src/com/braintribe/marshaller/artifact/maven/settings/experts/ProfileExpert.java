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

import com.braintribe.model.artifact.maven.settings.Profile;

public class ProfileExpert extends AbstractSettingsExpert {

	public static Profile read(SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		Profile result = create( context, Profile.T);
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ID:
							result.setId( extractString(context, reader));
						break;
						case ACTIVATION:
							result.setActivation( ActivationExpert.read(context,  reader));
							break;
						case REPOSITORIES:
							result.setRepositories( RepositoriesExpert.read(context, reader));
							break;
						case PROPERTIES:
							result.setProperties( PropertiesExpert.read( context, reader));
							break;
						case PLUGIN_REPOSITORIES:
							result.setPluginRepositories( PluginRepositoriesExpert.read( context, reader));
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
