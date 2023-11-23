// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk.space;

import java.util.function.BiConsumer;

import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.ResolutionVisitingContract;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class ResolutionVisitingSpace implements ResolutionVisitingContract {
	
	@Override
	public BiConsumer<Solution, Dependency> solutionDependencyVisitor() {
		return (s,d) -> {};
	}
	
}
