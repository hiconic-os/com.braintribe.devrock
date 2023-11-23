// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.generic.GenericEntity;

public class VersionRangeCodec implements Codec<GenericEntity, String> {

	@Override
	public String encode(GenericEntity value) throws CodecException {	
		if (value instanceof VersionRange == false) {
			String msg ="can only convert version ranges";
			throw new CodecException(msg);
		}		
		return VersionRangeProcessor.toString( (VersionRange) value);		
	}

	@Override
	public GenericEntity decode(String encodedValue) throws CodecException {
		try {
			return VersionRangeProcessor.createFromString(encodedValue);
		} catch (VersionProcessingException e) {
			String msg ="cannot convert string [" + encodedValue + "] to a version range";
			throw new CodecException(msg);
		}
	}

	@Override
	public Class<GenericEntity> getValueClass() {		
		return GenericEntity.class;
	}

}
