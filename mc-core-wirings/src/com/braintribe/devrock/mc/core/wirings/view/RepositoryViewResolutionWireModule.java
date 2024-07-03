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
