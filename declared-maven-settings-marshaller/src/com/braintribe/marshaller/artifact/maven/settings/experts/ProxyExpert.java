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

import com.braintribe.model.artifact.maven.settings.Proxy;

public class ProxyExpert extends AbstractSettingsExpert {
	public static Proxy read(SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		Proxy result = create( context, Proxy.T);
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
