package com.braintribe.build.artifacts.mc.wire.classwalk;

import java.util.List;

import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;

public enum ClasspathResolverWireModule implements WireModule {
	INSTANCE;

	@Override
	public List<WireModule> dependencies() {
		return WireModule.super.dependencies();
	}

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {	
		WireModule.super.configureContext(contextBuilder);
	}

}
