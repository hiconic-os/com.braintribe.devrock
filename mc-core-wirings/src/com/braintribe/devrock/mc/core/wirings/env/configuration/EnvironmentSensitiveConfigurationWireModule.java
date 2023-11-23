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
