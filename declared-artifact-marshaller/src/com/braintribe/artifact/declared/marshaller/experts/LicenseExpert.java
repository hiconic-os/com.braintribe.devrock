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
package com.braintribe.artifact.declared.marshaller.experts;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.artifact.declared.marshaller.PomReadContext;
import com.braintribe.model.artifact.declared.License;

public class LicenseExpert extends AbstractPomExpert implements HasPomTokens {

	public static License read(PomReadContext context, XMLStreamReader reader) throws XMLStreamException{
		License license = create( context, License.T);
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case NAME : {
							license.setName( extractString(context, reader));
							break;
						}
						case URL : {
							license.setUrl( extractString(context, reader));
							break;
						}
						case DISTRIBUTION : {
							license.setDistribution( extractString(context, reader));							
							break;
						}
						
						default:
							skip(reader);
							break;
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return license;
				}
				default:
					break;
			}
			reader.next();
		}
		return null;
	}

}
