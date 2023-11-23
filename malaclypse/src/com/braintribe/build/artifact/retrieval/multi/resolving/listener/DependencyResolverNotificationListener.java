// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving.listener;

import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;

public interface DependencyResolverNotificationListener {

	void acknowledgeRedirection(String walkScopeId, Part source, Solution target);
}
