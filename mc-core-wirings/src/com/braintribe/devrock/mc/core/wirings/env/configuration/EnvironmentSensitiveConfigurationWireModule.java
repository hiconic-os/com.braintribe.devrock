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
package com.braintribe.devrock.mc.core.wirings.env.configuration;

import java.util.List;

import com.braintribe.devrock.mc.core.wirings.configuration.RepositoryConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.devrock.contract.ProblemAnalysisContract;
import com.braintribe.devrock.mc.core.wirings.env.configuration.space.EnvironmentSensitiveConfigurationSpace;
import com.braintribe.devrock.mc.core.wirings.env.configuration.space.EnvironmentSensitiveProblemAnalysisSpace;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.venv.VirtualEnviromentWireModule;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.util.Lists;

/**
 * a {@link WireModule} to run a configuration depending on the environment (currently : location of current directory) 
 * 
 * @author pit / dirk
 *
 */
public class EnvironmentSensitiveConfigurationWireModule implements WireTerminalModule<RepositoryConfigurationContract> {

	public static EnvironmentSensitiveConfigurationWireModule INSTANCE = new EnvironmentSensitiveConfigurationWireModule();

	private final VirtualEnvironmentContract virtualEnvironmentContract;

	public EnvironmentSensitiveConfigurationWireModule() {
		this.virtualEnvironmentContract = null;
	}

	public EnvironmentSensitiveConfigurationWireModule(VirtualEnvironment virtualEnvironment) {
		this(() -> virtualEnvironment);
	}

	public EnvironmentSensitiveConfigurationWireModule(VirtualEnvironmentContract virtualEnvironmentContract) {
		this.virtualEnvironmentContract = virtualEnvironmentContract;
	}

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract(RepositoryConfigurationContract.class, EnvironmentSensitiveConfigurationSpace.class);
		contextBuilder.bindContract(ProblemAnalysisContract.class, EnvironmentSensitiveProblemAnalysisSpace.class);

		if (virtualEnvironmentContract != null)
			contextBuilder.bindContract(VirtualEnvironmentContract.class, virtualEnvironmentContract);
	}

	@Override
	public List<WireModule> dependencies() {
		return Lists.list(VirtualEnviromentWireModule.INSTANCE, RepositoryConfigurationWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE);
	}

}
