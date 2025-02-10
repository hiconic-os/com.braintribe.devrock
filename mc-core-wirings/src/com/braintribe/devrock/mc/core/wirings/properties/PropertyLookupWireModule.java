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
package com.braintribe.devrock.mc.core.wirings.properties;

import java.util.Collections;
import java.util.List;

import com.braintribe.devrock.mc.core.wirings.devrock.contract.ProblemAnalysisContract;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.properties.contract.PropertyLookupContract;
import com.braintribe.devrock.mc.core.wirings.properties.contract.PropertyLookupModuleContract;
import com.braintribe.devrock.mc.core.wirings.properties.space.PropertyLookupSupportSpace;
import com.braintribe.devrock.mc.core.wirings.venv.VirtualEnviromentWireModule;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.space.ContractResolution;
import com.braintribe.wire.api.space.ContractSpaceResolver;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.impl.properties.PropertyLookups;

/**
 * simple module for the {@link ProblemAnalysisContract}, more important : {@link EnvironmentSensitiveConfigurationWireModule}
 * @author pit
 *
 */
public enum PropertyLookupWireModule implements WireModule {
	INSTANCE;
		
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireModule.super.configureContext(contextBuilder);
		
		PropertyLookupContractResolver resolver = new PropertyLookupContractResolver();
		contextBuilder.bindContracts(resolver);
		contextBuilder.bindContract(PropertyLookupModuleContract.class, resolver);
		contextBuilder.autoLoad(PropertyLookupSupportSpace.class);
	}

	class PropertyLookupContractResolver implements ContractSpaceResolver, PropertyLookupModuleContract {
		
		private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
		
		@Override
		public void init(VirtualEnvironment virtualEnvironment) {
			this.virtualEnvironment = virtualEnvironment;
		}
		
		@Override
		public ContractResolution resolveContractSpace(Class<? extends WireSpace> contractSpaceClass) {
			if (PropertyLookupContract.class.isAssignableFrom(contractSpaceClass)) {
				return n -> PropertyLookups.create(contractSpaceClass, var -> virtualEnvironment.getEnv(var));
			}
			return null;
		}
	}
	
	@Override
	public List<WireModule> dependencies() {
		return Collections.singletonList(VirtualEnviromentWireModule.INSTANCE);
	}

}
