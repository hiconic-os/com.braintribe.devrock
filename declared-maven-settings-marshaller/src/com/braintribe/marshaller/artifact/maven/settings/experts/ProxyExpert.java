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
