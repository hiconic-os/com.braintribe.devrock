// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.clash.merger.listener;

import java.util.Set;

import com.braintribe.build.artifact.walk.multi.clash.merger.DependencyMerger;
import com.braintribe.model.artifact.Dependency;

/**
 * listener for reports from the {@link DependencyMerger}
 * @author pit
 *
 */
public interface DependencyMergerNotificationListener {

	void acknowledgeMerges( String walkScopeId, Set<Dependency> mergedDependencies);
}
