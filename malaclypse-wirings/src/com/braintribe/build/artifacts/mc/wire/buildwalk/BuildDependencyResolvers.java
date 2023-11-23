// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk;

import java.util.function.Consumer;

import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.CodebaseAwareBuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.IntransitiveResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.space.CodebaseAwareBuildDependencyResolutionConfigurationSpace;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextBuilder;

/**
 * @deprecated use {@link BuildDependencyResolverWireModule} instead
 * @author Dirk Scheffler
 *
 */
@Deprecated
public interface BuildDependencyResolvers {

	/**
	 * @deprecated use {@link #standard(Consumer)} instead
	 */
	@Deprecated
	public static WireContext<BuildDependencyResolutionContract> wire(Consumer<WireContextBuilder<BuildDependencyResolutionContract>> configurer) {
		return standard(configurer);
	}
	
	public static WireContext<BuildDependencyResolutionContract> standard(Consumer<WireContextBuilder<BuildDependencyResolutionContract>> configurer) {
		WireContextBuilder<BuildDependencyResolutionContract> builder = Wire.context(BuildDependencyResolutionContract.class)
				.bindContracts(BuildDependencyResolvers.class.getPackage().getName());
		
		configurer.accept(builder);
		
		return builder.build();
	}
	
	
	public static WireContext<CodebaseAwareBuildDependencyResolutionContract> codebaseAware(Consumer<WireContextBuilder<CodebaseAwareBuildDependencyResolutionContract>> configurer) {
		WireContextBuilder<CodebaseAwareBuildDependencyResolutionContract> builder = Wire.context(CodebaseAwareBuildDependencyResolutionContract.class)
				.bindContracts(BuildDependencyResolvers.class.getPackage().getName())
				.bindContract(IntransitiveResolutionContract.class, CodebaseAwareBuildDependencyResolutionConfigurationSpace.class);
		
		configurer.accept(builder);
		
		return builder.build();
	}
}
