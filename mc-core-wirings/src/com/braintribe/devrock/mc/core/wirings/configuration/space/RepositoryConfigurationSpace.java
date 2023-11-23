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
