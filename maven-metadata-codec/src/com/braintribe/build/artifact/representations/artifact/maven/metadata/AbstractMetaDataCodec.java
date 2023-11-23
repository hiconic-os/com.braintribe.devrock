// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.metadata;

import org.w3c.dom.Element;

public abstract class AbstractMetaDataCodec {
	protected void attachAsTextContent( String value, String tag, Element parent) {
		Element element = parent.getOwnerDocument().createElement( tag);
		element.setTextContent( value);
		parent.appendChild(element);
	}
}
