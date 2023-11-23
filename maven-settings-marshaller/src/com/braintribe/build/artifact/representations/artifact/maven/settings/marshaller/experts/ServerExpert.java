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

import com.braintribe.model.maven.settings.Configuration;
import com.braintribe.model.maven.settings.Server;

public class ServerExpert extends AbstractSettingsExpert {
	public static Server read(SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		Server result = Server.T.create();
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ID:
							result.setId( extractString( context, reader));
						break;
						case USERNAME:
							result.setUsername(extractString( context, reader));
							break;
						case PASSWORD:
							result.setPassword(extractString(context, reader));
							break;
						case FILE_PERMISSIONS:
							result.setFilePermissions(extractString(context, reader));
							break;
						case DIRECTORY_PERMISSIONS:
							result.setDirectoryPermissions( extractString(context, reader));
							break;
						case PASSPHRASE:
							result.setPassphrase( extractString(context, reader));
							break;
						case PRIVATE_KEY : 
							result.setPrivateKey( extractString(context, reader));
							break;
						case CONFIGURATION:
							String any = extractString(context, reader);
							Configuration configuration = Configuration.T.create();
							configuration.setAny(any);
							result.setConfiguration( configuration);
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
