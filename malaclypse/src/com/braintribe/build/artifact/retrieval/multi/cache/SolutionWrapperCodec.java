// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.cache;

import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class SolutionWrapperCodec extends HashSupportWrapperCodec<Solution>{

	@Override
	protected int entityHashCode(Solution e) {		
		return new String( e.getGroupId() + ":" + e.getArtifactId() + "#" + VersionProcessor.toString(e.getVersion())).hashCode();
	}

	@Override
	protected boolean entityEquals(Solution e1, Solution e2) {
		return ArtifactProcessor.solutionEquals(e1, e2);
	}
}
