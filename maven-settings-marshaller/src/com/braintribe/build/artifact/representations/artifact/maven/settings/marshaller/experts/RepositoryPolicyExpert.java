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

import com.braintribe.model.maven.settings.RepositoryPolicy;

public class RepositoryPolicyExpert extends AbstractSettingsExpert {


	public static RepositoryPolicy read(SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		RepositoryPolicy result = RepositoryPolicy.T.create();
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ENABLED:
							result.setEnabled( Boolean.valueOf( extractString(context, reader)));
							break;
						case CHECKSUM_POLICY:
							result.setChecksumPolicy( extractString(context, reader));
							break;
						case UPDATE_POLICY:
							result.setUpdatePolicy( extractString(context,  reader));
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
