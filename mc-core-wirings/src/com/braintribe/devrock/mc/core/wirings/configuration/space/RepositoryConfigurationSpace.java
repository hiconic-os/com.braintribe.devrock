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
package com.braintribe.devrock.mc.core.wirings.configuration.space;

import com.braintribe.devrock.mc.core.configuration.ConfigurableRepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.DevelopmentEnvironmentContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationLocatorContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.StandardRepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.config.wire.contract.ModeledConfigurationContract;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class RepositoryConfigurationSpace implements RepositoryConfigurationContract, StandardRepositoryConfigurationContract {
	
	@Import
	private DevelopmentEnvironmentContract developmentEnvironment;
	
	@Import
	private VirtualEnvironmentContract virtualEnvironment;
	
	@Import
	private RepositoryViewResolutionSpace repositoryViewResolution;
	
	@Import
	private RepositoryConfigurationLocatorContract repositoryConfigurationLocator;
	
	@Override
	public Maybe<RepositoryConfiguration> repositoryConfiguration() {
		return configurableRepositoryConfigurationLoader().get();
	}
	
	@Managed
	private ConfigurableRepositoryConfigurationLoader configurableRepositoryConfigurationLoader() {
		ConfigurableRepositoryConfigurationLoader bean = new ConfigurableRepositoryConfigurationLoader();
		bean.setDevelopmentEnvironmentRoot(developmentEnvironment.developmentEnvironmentRoot());
		bean.setVirtualEnvironment(virtualEnvironment.virtualEnvironment());
		bean.setLocator(repositoryConfigurationLocator.repositoryConfigurationLocator());
		return bean;
	}
}
