// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts;

import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.model.artifact.Exclusion;

public class ExclusionsExpert extends AbstractPomExpert implements HasPomTokens {

	public static Set<Exclusion> read(PomReadContext context, XMLStreamReader reader) throws XMLStreamException {
		Set<Exclusion> exclusions = new HashSet<>();
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case EXCLUSION : {
							exclusions.add( ExclusionExpert.read(context, reader));
							break;
						}
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return exclusions;
				}
				default:
					break;
			}
			reader.next();
		}
		return null;
	}

}
