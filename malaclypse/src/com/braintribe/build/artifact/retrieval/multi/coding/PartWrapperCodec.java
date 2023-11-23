// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.coding;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

/**
 * wrapper codec for {@link CodingSet} or {@link CodingMap} based structures with {@link Solution}
 * @author pit
 *
 */
public class PartWrapperCodec extends HashSupportWrapperCodec<Part> {
	
	public PartWrapperCodec() {
		super( true);
	}

	@Override
	protected int entityHashCode(Part e) {	
		return NameParser.buildName((Solution) e).hashCode();		
	}

	@Override
	protected boolean entityEquals(Part e1, Part e2) {
		
		if (!e1.getGroupId().equalsIgnoreCase(e2.getGroupId()))
			return false;
		if (!e1.getArtifactId().equalsIgnoreCase(e2.getArtifactId()))
			return false;
		
		if (!VersionProcessor.matches(e1.getVersion(), e2.getVersion()))
			return false;		
		return true;
	}

}
