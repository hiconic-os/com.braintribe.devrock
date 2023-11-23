// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk.space;

import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.GeneralConfigurationContract;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class GeneralConfigurationSpace implements GeneralConfigurationContract {
	@Override
	public VirtualEnvironment virtualEnvironment() {
		return StandardEnvironment.INSTANCE;
	}
	
	@Override
	public boolean lenient() {
		return false;
	}
	
	public boolean walkParentStructure() {
		return true;
	}
}
