// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
