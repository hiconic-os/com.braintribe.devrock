// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.coding;

import com.braintribe.cc.lcd.HashSupportWrapperCodec;

/**
 * simple codec for string keys, yet the string should be treated as case insensitive 
 * 
 * @author pit
 *
 */
public class CaseInsensitiveHashSupportWrapperCodec extends HashSupportWrapperCodec<String> {

	@Override
	protected int entityHashCode(String e) {		
		return e.toUpperCase().hashCode();
	}

	@Override
	protected boolean entityEquals(String e1, String e2) {		
		return e1.equalsIgnoreCase(e2);
	}

	
}
