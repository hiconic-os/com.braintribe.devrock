package com.braintribe.build.artifact.api;

import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.version.VersionRange;

public class IdentificationRangedArtifact extends AbstractRangedArtifact {
	private Identification identification;
	private VersionRange versionRange;
	
	public IdentificationRangedArtifact(Identification identification, VersionRange versionRange) {
		this.identification = identification;
		this.versionRange = versionRange;
	}
	
	@Override
	public String getGroupId() {
		return identification.getGroupId();
	}
	
	@Override
	public String getArtifactId() {
		return identification.getArtifactId();
	}
	
	@Override
	public VersionRange getVersionRange() {
		return versionRange;
	}
}
