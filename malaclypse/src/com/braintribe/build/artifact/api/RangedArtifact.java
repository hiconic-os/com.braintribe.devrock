// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.api;

import com.braintribe.model.artifact.version.VersionRange;

public interface RangedArtifact {
	String getGroupId();
	String getArtifactId();
	VersionRange getVersionRange();
	
}
