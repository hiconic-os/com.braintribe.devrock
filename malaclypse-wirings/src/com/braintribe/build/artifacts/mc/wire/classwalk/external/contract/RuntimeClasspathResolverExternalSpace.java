package com.braintribe.build.artifacts.mc.wire.classwalk.external.contract;

import java.util.Set;

import com.braintribe.model.malaclypse.cfg.denotations.scopes.Scope;

public class RuntimeClasspathResolverExternalSpace implements ClasspathResolverExternalContract {

	@Override
	public Set<Scope> scopes() {
		return Scopes.runtimeScopes();
		
	}

}
