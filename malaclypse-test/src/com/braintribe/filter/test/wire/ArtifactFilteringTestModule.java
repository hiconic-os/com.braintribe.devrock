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

import java.util.List;

import com.braintribe.build.artifacts.mc.wire.buildwalk.BuildDependencyResolverWireModule;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.FilterConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.GeneralConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.MavenSettingsContract;
import com.braintribe.filter.test.wire.contract.FilteringTestConfigurationContract;
import com.braintribe.filter.test.wire.space.FilteringFilterConfigurationSpace;
import com.braintribe.filter.test.wire.space.FilteringGeneralConfigurationSpace;
import com.braintribe.filter.test.wire.space.FilteringMavenSettingsSpace;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.util.Lists;

public class ArtifactFilteringTestModule implements WireTerminalModule<BuildDependencyResolutionContract>{
	
	private FilteringTestConfigurationSpace configuration;
	
	public ArtifactFilteringTestModule( FilteringTestConfigurationSpace cfg) {
		this.configuration = cfg;
	}

	@Override
	public List<WireModule> dependencies() {
		return Lists.list( BuildDependencyResolverWireModule.DEFAULT);
	}

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		contextBuilder
		// overload ve
		.bindContract(GeneralConfigurationContract.class, FilteringGeneralConfigurationSpace.class)
		// overload the settings contract 
		.bindContract( MavenSettingsContract.class, FilteringMavenSettingsSpace.class)
		// overload the filter 
		.bindContract( FilterConfigurationContract.class, FilteringFilterConfigurationSpace.class)
		// inject cfg
		.bindContract( FilteringTestConfigurationContract.class, configuration)
		// done		
		.build();
	}

	
	
}
