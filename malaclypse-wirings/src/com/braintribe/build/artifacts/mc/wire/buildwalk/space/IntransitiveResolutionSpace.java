// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk.space;

import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.IntransitiveResolutionContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class IntransitiveResolutionSpace implements IntransitiveResolutionContract {
	@Import
	private BuildDependencyResolutionSpace buildDependencyResolutionSpace;
	
	@Override
	public DependencyResolver intransitiveDependencyResolver() {
		return buildDependencyResolutionSpace.standardDependencyResolver();
	}
}
