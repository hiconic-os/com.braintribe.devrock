// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.metadata;

import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;

public class VersionCodingCodec extends HashSupportWrapperCodec<Version> {

	@Override
	protected int entityHashCode(Version e) {		
		return VersionProcessor.toString(e).hashCode();				
	}

	@Override
	protected boolean entityEquals(Version e1, Version e2) {	
		return VersionProcessor.hardMatches(e1, e2);
	}

	
}
