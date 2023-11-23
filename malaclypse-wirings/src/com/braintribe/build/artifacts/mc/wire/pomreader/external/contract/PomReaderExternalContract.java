// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.pomreader.external.contract;

import java.util.function.Function;

import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.space.WireSpace;

public interface PomReaderExternalContract extends WireSpace {
	Function<DependencyResolver, DependencyResolver> dependencyResolverEnricher();
	VirtualEnvironment virtualEnvironment();
	default String globalMalaclypseScopeId() { return null;};
}
