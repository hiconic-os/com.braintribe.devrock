// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings.marshaller.experts;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.model.maven.settings.Settings;



public class SettingsExpert extends AbstractSettingsExpert {

	public static Settings read(SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		Settings settings = Settings.T.create();

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
