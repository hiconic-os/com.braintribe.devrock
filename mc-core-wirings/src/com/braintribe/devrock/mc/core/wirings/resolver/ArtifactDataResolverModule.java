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
package com.braintribe.devrock.mc.core.wirings.resolver;

import java.util.List;

import com.braintribe.devrock.mc.core.wirings.backend.ArtifactDataBackendModule;
import com.braintribe.devrock.mc.core.wirings.configuration.RepositoryConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.VirtualEnviromentWireModule;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.util.Lists;

/**
 * a {@link WireTerminalModule} for the {@link ArtifactDataResolverContract} (middle tier)
 * @author pit/dirk
 *
 */
public enum ArtifactDataResolverModule implements WireTerminalModule<ArtifactDataResolverContract> {
	INSTANCE;

	@Override
	public List<WireModule> dependencies() {	
		return Lists.list( ArtifactDataBackendModule.INSTANCE, VirtualEnviromentWireModule.INSTANCE, RepositoryConfigurationWireModule.INSTANCE);
	}
}
