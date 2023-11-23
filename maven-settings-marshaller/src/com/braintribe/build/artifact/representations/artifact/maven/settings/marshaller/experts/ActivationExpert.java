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

import com.braintribe.model.maven.settings.Activation;

public class ActivationExpert extends AbstractSettingsExpert{
	
	public static Activation read(SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		Activation result = Activation.T.create();
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
