// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.exclusion;

import java.util.Set;

import com.braintribe.build.artifact.retrieval.multi.coding.IdentificationWrapperCodec;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.model.artifact.Identification;

/**
 * {@link CodingSet} based container for exclusions (as {@link Identification}
 * @author Pit
 *
 */
public class SetBasedExclusionContainer implements ExclusionContainer {

	Set<Identification> container = CodingSet.createHashSetBased( new IdentificationWrapperCodec());

	@Override
	public boolean contains(Identification identification) {	
		return container.contains(identification);
	}

	@Override
	public void addAll(Set<Identification> identifications) {
		container.addAll( identifications);
	}
	

}
