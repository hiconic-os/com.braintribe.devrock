package com.braintribe.build.artifacts.mc.wire.classwalk.external.contract;

import java.util.Set;

import com.braintribe.model.malaclypse.cfg.denotations.scopes.Scope;

/**
 * simple, default space for a compile walk 
 * 
 * @author pit
 *
 */
public class CompileClasspathResolverExternalSpace implements ClasspathResolverExternalContract {

	@Override
	public Set<Scope> scopes() {
		return Scopes.compileScopes();
		
	}

}
