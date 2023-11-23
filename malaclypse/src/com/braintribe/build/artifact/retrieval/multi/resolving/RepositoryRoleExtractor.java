// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;



import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.ravenhurst.data.RepositoryRole;

public class RepositoryRoleExtractor {

	public static RepositoryRole getRelevantRole( Dependency dependency){
		VersionRange dependencyRange = dependency.getVersionRange();
		RepositoryRole repositoryRole;
		if (dependencyRange.getInterval()) {
			repositoryRole = RepositoryRole.both;
		} 
		else {
			String versionRangeAsString = VersionRangeProcessor.toString(dependencyRange);
			if (versionRangeAsString.toUpperCase().endsWith("-SNAPSHOT")) {
				repositoryRole = RepositoryRole.snapshot;
			}
			else {
				repositoryRole = RepositoryRole.release;
			}
		}
		return repositoryRole;
	}
	
	public static RepositoryRole getRelevantRole( Solution solution){		
		Version version = solution.getVersion();
		return getRelevantRole(version);
	}
	
	public static RepositoryRole getRelevantRole( Version version) {
		RepositoryRole repositoryRole = RepositoryRole.both;		
		String versionRangeAsString = VersionProcessor.toString( version);
		if (versionRangeAsString.toUpperCase().endsWith("-SNAPSHOT")) {
			repositoryRole = RepositoryRole.snapshot;
		}
		else {
			repositoryRole = RepositoryRole.release;
		}
		return repositoryRole;
	}
	
}
