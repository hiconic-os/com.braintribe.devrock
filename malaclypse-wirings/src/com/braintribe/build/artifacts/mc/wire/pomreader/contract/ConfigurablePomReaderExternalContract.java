// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.pomreader.contract;

import java.util.function.Function;

import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifacts.mc.wire.pomreader.external.contract.PomReaderExternalContract;
import com.braintribe.cfg.Configurable;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

public class ConfigurablePomReaderExternalContract implements PomReaderExternalContract {

	private Function<DependencyResolver, DependencyResolver> dependencyResolverEnricher;
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	
	@Configurable
	public void setDependencyResolverEnricher(Function<DependencyResolver, DependencyResolver> dependencyResolverEnricher) {
		this.dependencyResolverEnricher = dependencyResolverEnricher;
	}
	
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	@Override
	public Function<DependencyResolver, DependencyResolver> dependencyResolverEnricher() {
		return dependencyResolverEnricher;
	}

	@Override
	public VirtualEnvironment virtualEnvironment() {
		return virtualEnvironment;
	}
}
