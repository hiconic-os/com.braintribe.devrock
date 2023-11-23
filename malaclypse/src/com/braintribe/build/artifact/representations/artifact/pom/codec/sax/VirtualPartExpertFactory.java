// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.codec.sax;

import com.braintribe.xml.parser.experts.ContentExpert;
import com.braintribe.xml.parser.registry.ContentExpertFactory;

public class VirtualPartExpertFactory implements ContentExpertFactory {

	@Override
	public ContentExpert newInstance() {
		return new VirtualPartExpert();
	}

}
