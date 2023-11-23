// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.coding;

import com.braintribe.build.artifact.walk.multi.scope.ScopeControl;
import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.DependencyScope;

/**
 * wrapper codec for scopes - see {@link ScopeControl}
 * @author pit
 *
 */
public class DependencyScopeWrapperCodec extends HashSupportWrapperCodec<DependencyScope> {
	
	public DependencyScopeWrapperCodec() {
		super(true);
	}

	@Override
	protected int entityHashCode(DependencyScope e) {
		return e.getName().hashCode();
	}

	@Override
	protected boolean entityEquals(DependencyScope e1, DependencyScope e2) {
		return e1.getName().equalsIgnoreCase(e2.getName());
	}

}
