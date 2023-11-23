// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.api;

import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.VersionRange;

public abstract class AbstractRangedArtifact implements RangedArtifact {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getArtifactId() == null) ? 0 : getArtifactId().hashCode());
		result = prime * result + ((getGroupId() == null) ? 0 : getGroupId().hashCode());
		result = prime * result + ((getVersionRange() == null) ? 0 : hashCode(getVersionRange()));
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RangedArtifact other = (RangedArtifact) obj;
		if (getArtifactId() == null) {
			if (other.getArtifactId() != null)
				return false;
		} else if (!getArtifactId().equals(other.getArtifactId()))
			return false;
		if (getGroupId() == null) {
			if (other.getGroupId() != null)
				return false;
		} else if (!getGroupId().equals(other.getGroupId()))
			return false;
		if (getVersionRange() == null) {
			if (other.getVersionRange() != null)
				return false;
		} else if (!VersionRangeProcessor.equals(getVersionRange(), other.getVersionRange()))
			return false;
		
		return true;
	}
	
	static int hashCode(VersionRange versionRange) {
		// TODO: better hashcode for VersionRange
		return 0;
	}
}
