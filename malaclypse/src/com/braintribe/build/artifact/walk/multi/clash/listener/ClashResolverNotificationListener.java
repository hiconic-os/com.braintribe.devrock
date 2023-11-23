// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.clash.listener;

import java.util.List;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

public interface ClashResolverNotificationListener {
	void acknowledgeDependencyClashes( String walkScopeId, Dependency dependency, List<Dependency> clashes);
	void acknowledgeSolutionClashes( String walkScopeId, Solution solution, List<Solution> clashes);
	void acknowledgeDependencyClashResolving( String walkScopeId, Dependency dependency);
	void acknowledgeSolutionClashResolving( String walkScopeId,  Solution solution);
}
