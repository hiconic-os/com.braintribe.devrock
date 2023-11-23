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

import com.braintribe.model.artifact.maven.settings.Settings;



public class SettingsExpert extends AbstractSettingsExpert {

	public static Settings read(SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		Settings settings = create( context, Settings.T);

		// called at the start element event, must get passed this - because we SKIP anyother tag!!
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case LOCAL_REPOSITORY:
							settings.setLocalRepository( extractString(context, reader));
							break;
						case INTERACTIVE_MODE:
							settings.setInteractiveMode( Boolean.valueOf( extractString( context, reader)));
							break;
						case USE_PLUGIN_REGISTRY: 
							settings.setUsePluginRegistry( Boolean.valueOf(extractString(context, reader)));
							break;
						case OFFLINE:
							settings.setOffline( Boolean.valueOf(extractString(context, reader)));
							break;
						case SERVERS:
							settings.setServers( ServersExpert.read( context, reader));
							break;
						case MIRRORS:
							settings.setMirrors( MirrorsExpert.read( context, reader));
							break;
						case PROXIES:
							settings.setProxies( ProxiesExpert.read( context, reader));
							break;
						case PROFILES:
							settings.setProfiles( ProfilesExpert.read( context, reader));
							break;
						case ACTIVE_PROFILES:
							settings.setActiveProfiles( ActiveProfilesExpert.read( context, settings.getProfiles(), reader));
							break;
						case PLUGIN_GROUPS: 
							settings.setPluginGroups( PluginGroupsExpert.read( context, reader));
							break;
						default:
							skip(reader);
							break;
					}
				}
				break;
				case XMLStreamConstants.END_ELEMENT : {
					return settings;
				}
				default:
					break;
			}
			reader.next();
		}
		return null;
	}

}
