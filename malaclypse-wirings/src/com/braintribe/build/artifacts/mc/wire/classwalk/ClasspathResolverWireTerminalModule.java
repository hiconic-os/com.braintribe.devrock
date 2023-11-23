package com.braintribe.build.artifacts.mc.wire.classwalk;

import java.util.List;

import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

public class ClasspathResolverWireTerminalModule implements WireTerminalModule<ClasspathResolverContract> {	

	
	private ClasspathResolverExternalContract externalContract;
	
	public ClasspathResolverWireTerminalModule(ClasspathResolverExternalContract externalContract) {
		this.externalContract = externalContract;
	}
	
	@Override
	public List<WireModule> dependencies() {
		return WireTerminalModule.super.dependencies();
	}

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);		
		contextBuilder.bindContract( ClasspathResolverExternalContract.class, externalContract);
	}

	/*
	public static void main(String[] args) {
		ClasspathResolverExternalContract externalContract = new ConfigurableClasspathExternalContract();
		....
		WireContext<ClasspathResolverContract> context = Wire.context( new ClasspathResolverWireTerminalModule( externalContract));
		contex.contract()... 
	}
	*/
}
