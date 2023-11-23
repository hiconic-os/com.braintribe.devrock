// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.filter.test.wire;

import java.io.File;

import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.filter.test.wire.contract.FilteringTestConfigurationContract;
import com.braintribe.ve.api.VirtualEnvironment;

public class FilteringTestConfigurationSpace implements FilteringTestConfigurationContract {
	
	private OverrideableVirtualEnvironment overridableVirtualEnvironment;
	private File localRepository;

	@Configurable @Required
	public void setOverridableVirtualEnvironment(OverrideableVirtualEnvironment overridableVirtualEnvironment) {
		this.overridableVirtualEnvironment = overridableVirtualEnvironment;
	}	
	@Override
	public VirtualEnvironment virtualEnvironment() {
		return overridableVirtualEnvironment;
	}
	
	@Configurable @Required
	public void setLocalRepository(File localRepository) {
		this.localRepository = localRepository;
	}
	@Override
	public File localRepository() {
		return localRepository;
	}

	
}
