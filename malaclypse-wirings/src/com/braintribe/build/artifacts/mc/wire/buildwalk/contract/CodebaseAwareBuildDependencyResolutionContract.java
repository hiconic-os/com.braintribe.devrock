// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk.contract;

import com.braintribe.build.artifact.codebase.reflection.CodebaseReflection;

public interface CodebaseAwareBuildDependencyResolutionContract extends BuildDependencyResolutionContract {
	CodebaseReflection codebaseReflection();
}
