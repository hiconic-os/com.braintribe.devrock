// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.model.artifact.Property;

public class PropertyExpert extends AbstractPomExpert implements HasPomTokens {
	
	public static Property read(PomReadContext context, String tag, XMLStreamReader reader) throws XMLStreamException{
		Property property = Property.T.create();
		property.setName(tag);
		property.setRawValue( extractString(context, reader));
		return property;
	}

}
