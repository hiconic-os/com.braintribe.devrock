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
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.generic.GenericEntity;

public class VersionCodec implements Codec<GenericEntity, String> {

	@Override
	public String encode(GenericEntity value) throws CodecException {
		if (value instanceof Version == false) {
			String msg ="cannot only convert Version";
			throw new CodecException(msg);
		}
					
		return VersionProcessor.toString( (Version) value);
		
	}

	@Override
	public GenericEntity decode(String encodedValue) throws CodecException {
		try {
			return VersionProcessor.createFromString(encodedValue);
		} catch (VersionProcessingException e) {
			String msg ="cannot convert passed string [" + encodedValue + "] to a valid version";
			throw new CodecException(msg);
		}
	}

	@Override
	public Class<GenericEntity> getValueClass() {
		return GenericEntity.class;
	}

}
