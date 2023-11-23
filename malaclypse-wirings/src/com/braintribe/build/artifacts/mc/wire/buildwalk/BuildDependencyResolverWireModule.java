package com.braintribe.build.artifacts.mc.wire.buildwalk;

import java.util.function.Consumer;

import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.IntransitiveResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.space.CodebaseAwareBuildDependencyResolutionConfigurationSpace;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireTerminalModule;

public enum BuildDependencyResolverWireModule implements WireTerminalModule<BuildDependencyResolutionContract> {
	DEFAULT(BuildDependencyResolverWireModule::defaultConfigurer),
	CODEBASE(BuildDependencyResolverWireModule::codebaseConfigurer);

	private Consumer<WireContextBuilder<?>> configurer;

	private BuildDependencyResolverWireModule(Consumer<WireContextBuilder<?>> configurer) {
		this.configurer = configurer;
	}
	
	@Override
	public Class<BuildDependencyResolutionContract> contract() {
		return BuildDependencyResolutionContract.class;
	}
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		configurer.accept(contextBuilder);
	}
	
	private static void defaultConfigurer(WireContextBuilder<?> contextBuilder) {
		// noop
	}
	
	private static void codebaseConfigurer(WireContextBuilder<?> contextBuilder) {
		contextBuilder.bindContract(IntransitiveResolutionContract.class, CodebaseAwareBuildDependencyResolutionConfigurationSpace.class);
	}

}
