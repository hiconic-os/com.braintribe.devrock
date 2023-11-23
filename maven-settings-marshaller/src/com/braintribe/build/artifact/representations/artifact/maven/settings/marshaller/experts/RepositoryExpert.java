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

import com.braintribe.model.maven.settings.Repository;

public class RepositoryExpert extends AbstractSettingsExpert {
	
	public static Repository read(SettingsMarshallerContext context, XMLStreamReader reader) throws XMLStreamException {
		Repository result = Repository.T.create();
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ID:
							result.setId( extractString(context, reader));
						break;
						case URL:
							result.setUrl( extractString(context, reader));
							break;
						case NAME:
							result.setName( extractString(context, reader));
							break;
						case RELEASES:
							result.setReleases( RepositoryPolicyExpert.read( context, reader));
							break;
						case SNAPSHOTS:
							result.setSnapshots( RepositoryPolicyExpert.read( context, reader));
							break;
						case LAYOUT:
							result.setLayout( extractString(context, reader));
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
