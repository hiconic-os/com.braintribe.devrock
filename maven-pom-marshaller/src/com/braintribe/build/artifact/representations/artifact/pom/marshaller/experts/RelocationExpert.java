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

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class RelocationExpert extends AbstractPomExpert implements HasPomTokens {
	
	public static Solution read(PomReadContext context, XMLStreamReader reader) throws XMLStreamException  {
		reader.next();
		Solution relocation = Solution.T.create();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case GROUPID : {
							relocation.setGroupId( extractString(context, reader));
							break;
						}
						case ARTIFACTID: {
							relocation.setArtifactId( extractString(context, reader));
							break;
						}
						case VERSION: {
							String versionAsString = extractString(context, reader);
							relocation.setVersion( VersionProcessor.createFromString(versionAsString));
							break;
						}
						default:
							skip(reader);
							break;
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return relocation;
				}
				default:
					break;
			}
			reader.next();
		}
		return null;
	}
}
