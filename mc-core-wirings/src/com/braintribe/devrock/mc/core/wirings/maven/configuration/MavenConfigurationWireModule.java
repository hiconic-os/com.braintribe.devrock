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
package com.braintribe.devrock.mc.core.wirings.maven.configuration;

import java.util.List;

import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.contract.MavenConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.space.MavenConfigurationSpace;
import com.braintribe.devrock.mc.core.wirings.venv.VirtualEnviromentWireModule;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.util.Lists;

/**
 * a {@link WireModule} to run a 'Maven' style configuration
 * 
 * @author pit / dirk
 *
 */
public class MavenConfigurationWireModule implements WireTerminalModule<RepositoryConfigurationContract> {

	public static MavenConfigurationWireModule INSTANCE = new MavenConfigurationWireModule();

	private final VirtualEnvironmentContract virtualEnvironmentContract;

	public MavenConfigurationWireModule() {
		this.virtualEnvironmentContract = null;
	}

	public MavenConfigurationWireModule(VirtualEnvironment virtualEnvironment) {
		this(() -> virtualEnvironment);
	}

	public MavenConfigurationWireModule(VirtualEnvironmentContract virtualEnvironmentContract) {
		this.virtualEnvironmentContract = virtualEnvironmentContract;
	}

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract(RepositoryConfigurationContract.class, MavenConfigurationSpace.class);
		contextBuilder.bindContract(MavenConfigurationContract.class, MavenConfigurationSpace.class);

		if (virtualEnvironmentContract != null)
			contextBuilder.bindContract(VirtualEnvironmentContract.class, virtualEnvironmentContract);
	}

	@Override
	public List<WireModule> dependencies() {
		return Lists.list(VirtualEnviromentWireModule.INSTANCE);
	}

}
