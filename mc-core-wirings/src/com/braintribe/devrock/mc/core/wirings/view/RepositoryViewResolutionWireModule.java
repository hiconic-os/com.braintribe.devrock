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
package com.braintribe.devrock.mc.core.wirings.view;

import java.util.Arrays;
import java.util.List;

import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.mc.core.wirings.view.contract.RepositoryViewResolutionContract;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

public class RepositoryViewResolutionWireModule implements WireTerminalModule<RepositoryViewResolutionContract> {
	
	private RepositoryConfiguration repositoryConfiguration;
	private VirtualEnvironment virtualEnvironment;
	
	public RepositoryViewResolutionWireModule(RepositoryConfiguration repositoryConfiguration) {
		this.repositoryConfiguration = repositoryConfiguration;
	}
	
	public RepositoryViewResolutionWireModule(RepositoryConfiguration repositoryConfiguration, VirtualEnvironment ve) {
		this.repositoryConfiguration = repositoryConfiguration;
		this.virtualEnvironment = ve;
	}


	@Override
	public List<WireModule> dependencies() {
		return Arrays.asList(TransitiveResolverWireModule.INSTANCE);
	}
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		if (virtualEnvironment != null) {
			contextBuilder.bindContract( VirtualEnvironmentContract.class, () -> virtualEnvironment);	
		}
		
		contextBuilder.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(repositoryConfiguration));
		
	}
}
