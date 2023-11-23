// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.cache;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class CacheKey {
	
	private Solution solution;
	private String versionAsString;
	
	public CacheKey( Solution solution) {
		this.solution = solution;
	}
	
	public Solution getSolution() {
		return solution;
	}

	private String getVersionAsString() {
		if (versionAsString == null) {
			versionAsString = VersionProcessor.toString( solution.getVersion());
		}
		return versionAsString;
	}
	@Override
	public int hashCode() {
		return (solution.getGroupId() + solution.getArtifactId() + getVersionAsString()).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CacheKey == false)
			return false;		
		CacheKey suspect = (CacheKey) obj;
		if (solution.getGroupId().equalsIgnoreCase( suspect.solution.getGroupId()) == false)
			return false;
		if (solution.getArtifactId().equalsIgnoreCase( suspect.solution.getArtifactId()) == false)
			return false;		
		return getVersionAsString().equalsIgnoreCase( suspect.getVersionAsString());			
	}	
	
}
