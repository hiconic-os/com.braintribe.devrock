package com.braintribe.build.artifacts.mc.wire.pomreader;

import com.braintribe.build.artifacts.mc.wire.pomreader.contract.PomReaderContract;
import com.braintribe.build.artifacts.mc.wire.pomreader.external.contract.PomReaderExternalContract;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireTerminalModule;

public class PomReaderWireTerminalModule implements WireTerminalModule<PomReaderContract> {
	

	private PomReaderExternalContract cfg;

	public PomReaderWireTerminalModule( PomReaderExternalContract cfg) {
		this.cfg = cfg;
	
	}

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract( PomReaderExternalContract.class, cfg);
	}
	
	
	
}
