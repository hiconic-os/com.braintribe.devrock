// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.clash.merger;



public class DependencyMergerFactoryImpl implements DependencyMergerFactory {

	@Override
	public DependencyMerger get() throws RuntimeException {
		return new DependencyMergerImpl();
	}

}
