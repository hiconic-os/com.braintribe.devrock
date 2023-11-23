package com.braintribe.build.ravenhurst.scanner;

import java.util.Collection;
import java.util.Date;

public class ChangedArtifacts {
	private Collection<String> artifacts;
	private Date lastUpdate;

	public ChangedArtifacts(Collection<String> artifacts, Date lastUpdate) {
		super();
		this.artifacts = artifacts;
		this.lastUpdate = lastUpdate;
	}

	public Collection<String> getArtifacts() {
		return artifacts;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}
}
