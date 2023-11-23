// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.coding;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.artifact.Identification;

/**
 * wrapper codec for the {@link CodingSet} or {@link CodingMap} based structures with {@link Identification} 
 * @author Pit
 *
 */
public class IdentificationWrapperCodec extends HashSupportWrapperCodec<Identification> {
	
	public IdentificationWrapperCodec() {
		super(true);
	}

	@Override
	protected int entityHashCode(Identification e) {
		return (e.getGroupId() + ":" + e.getArtifactId()).hashCode();
	}

	@Override
	protected boolean entityEquals(Identification e1, Identification e2) {
		if (!e1.getGroupId().equalsIgnoreCase(e2.getGroupId()))
			return false;
		if (!e1.getArtifactId().equalsIgnoreCase(e2.getArtifactId()))
			return false;
		return true;
	}

}
