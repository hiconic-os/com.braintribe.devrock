// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.api;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.VersionRange;

public abstract class RangedArtifacts {
	public static RangedArtifact from(Solution solution) {
		return new IdentificationRangedArtifact(solution, VersionRangeProcessor.createfromVersion(solution.getVersion()));
	}
	
	public static RangedArtifact from(Dependency dependency) {
		return new IdentificationRangedArtifact(dependency, dependency.getVersionRange());
	}

	private static RangedArtifact boundaryFloor = new AbstractRangedArtifact() {
		private VersionRange vr = VersionRangeProcessor.createFromString("0");
		
		@Override
		public String getGroupId() {
			return "<floor>";
		}

		@Override
		public String getArtifactId() {
			return "<floor>";
		}

		@Override
		public VersionRange getVersionRange() {
			return vr;
		}
		
	};
	
	public static RangedArtifact boundaryFloor() {
		return boundaryFloor;
	}
}
