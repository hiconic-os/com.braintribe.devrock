// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.scope;

import java.util.function.Function;

import com.braintribe.model.malaclypse.cfg.denotations.ScopeControlDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.DependencyScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.MagicScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.Scope;


/**
 * a factory to create a parameterized  {@link ScopeControl} as declared by the {@link ScopeControlDenotationType}
 * 
 * @author pit
 *
 */
public class ScopeControlFactory implements Function<ScopeControlDenotationType, ScopeControl>{

	@Override
	public ScopeControl apply(ScopeControlDenotationType scopeControlDenotationType) throws RuntimeException {
		ScopeControl scopeControl = new ScopeControl();
		scopeControl.setSkipOptional( scopeControlDenotationType.getSkipOptional());	
		for (Scope scope : scopeControlDenotationType.getScopes()) {
			if (scope instanceof MagicScope) {
				MagicScope magicScope = (MagicScope) scope;
				for (DependencyScope dependencyScope : magicScope.getScopes()) {
					dependencyScope.setName(dependencyScope.getName().toUpperCase());
					scopeControl.addScope( dependencyScope);
				}
			}
			else {
				DependencyScope dependencyScope = (DependencyScope) scope;
				dependencyScope.setName(dependencyScope.getName().toUpperCase());
				scopeControl.addScope( dependencyScope);
			}
		}
		return scopeControl;
	}

	

}
