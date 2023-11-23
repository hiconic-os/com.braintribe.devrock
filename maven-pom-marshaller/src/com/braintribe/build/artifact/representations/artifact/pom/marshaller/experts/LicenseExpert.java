// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.model.artifact.Distribution;
import com.braintribe.model.artifact.License;

public class LicenseExpert extends AbstractPomExpert implements HasPomTokens {

	public static License read(PomReadContext context, XMLStreamReader reader) throws XMLStreamException{
		License license = License.T.create();
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
							String value = extractString(context, reader);
							try {
								license.setDistribution( Distribution.valueOf(value));
							} catch (Exception e) {
								license.setDistribution(Distribution.unexpected);
							}
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
