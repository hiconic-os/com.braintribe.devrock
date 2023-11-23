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

import com.braintribe.model.maven.settings.Proxy;

public class ProxyExpert extends AbstractSettingsExpert {
	public static Proxy read(SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		Proxy result = Proxy.T.create();
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ID:
							result.setId( extractString(context, reader));
							break;
						case ACTIVE:
							result.setActive( Boolean.valueOf( extractString(context, reader)));
							break;
						case PROTOCOL:
							result.setProtocol( extractString(context, reader));
							break;
						case HOST:
							result.setHost( extractString(context, reader));
							break;
						case PORT:
							result.setPort( Integer.valueOf( extractString( context, reader)));
							break;
						case USERNAME:
							result.setUsername(extractString(context, reader));
							break;
						case PASSWORD:
							result.setPassword(extractString(context, reader));
							break;
						case NON_PROXY_HOSTS:
							result.setNonProxyHosts( extractString(context, reader));
							break;
						default:
							skip(reader);
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
