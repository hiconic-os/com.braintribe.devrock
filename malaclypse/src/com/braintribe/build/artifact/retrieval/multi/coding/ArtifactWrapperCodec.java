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
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

/**
 * wrapper codec for {@link CodingSet} or {@link CodingMap} based structures with {@link Solution}
 * @author pit
 *
 */
public class ArtifactWrapperCodec extends HashSupportWrapperCodec<Artifact> {
	
	public ArtifactWrapperCodec() {
		super( true);
	}

	@Override
	protected int entityHashCode(Artifact e) {
		return NameParser.buildName(e).hashCode();
	}

	@Override
	protected boolean entityEquals(Artifact e1, Artifact e2) {
		
		if (!e1.getGroupId().equalsIgnoreCase(e2.getGroupId()))
			return false;
		if (!e1.getArtifactId().equalsIgnoreCase(e2.getArtifactId()))
			return false;
	
		if (!VersionProcessor.matches(e1.getVersion(), e2.getVersion()))
			return false;
		return true;
	}

}
