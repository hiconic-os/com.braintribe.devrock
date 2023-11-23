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

import com.braintribe.model.artifact.maven.settings.Activation;

public class ActivationExpert extends AbstractSettingsExpert{
	
	public static Activation read(SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		Activation result = create( context, Activation.T);
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ACTIVE_BY_DEFAULT:
							result.setActiveByDefault( Boolean.valueOf( extractString(context, reader)));
							break;
						case JDK:
							result.setJdk( extractString(context, reader));
							break;
						case OS:
							result.setOs( ActivationOsExpert.read(context,  reader));
							break;
						case PROPERTY:
							result.setProperty( ActivationPropertyExpert.read( context, reader));
							break;
						case FILE:
							result.setFile( ActivationFileExpert.read( context, reader));
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
