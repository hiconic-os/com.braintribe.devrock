// ============================================================================
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
package com.braintribe.devrock.mc.core.configuration;

import java.io.File;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocation;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocator;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocatorContext;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationLoaded;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.config.yaml.ModeledYamlConfigurationLoader;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * This class loads {@link RepositoryConfiguration} from a location provided by a {@link RepositoryConfigurationLocator}.
 *     
 * @author Dirk Scheffler
 */
public class ConfigurableRepositoryConfigurationLoader implements Supplier<Maybe<RepositoryConfiguration>> {
	
	private File developmentEnvironmentRoot;
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	private RepositoryConfigurationLocator locator;
	
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	@Configurable
	public void setDevelopmentEnvironmentRoot(File developmentEnvironmentRoot) {
		this.developmentEnvironmentRoot = developmentEnvironmentRoot;
	}
	
	@Configurable
	public void setLocator(RepositoryConfigurationLocator locator) {
		this.locator = locator;
	}
	
	public RepositoryConfigurationLocator getLocator() {
		if (locator == null) {
			locator = RepositoryConfigurationLocators.buildDefault().done();
		}

		return locator;
	}
	
	@Override
	public Maybe<RepositoryConfiguration> get() {
		Maybe<RepositoryConfigurationLocation> locationMaybe = getLocator().locateRepositoryConfiguration(buildLocatorContext());
		
		if (locationMaybe.isUnsatisfied())
			return Maybe.empty(locationMaybe.whyUnsatisfied());
		
		RepositoryConfigurationLocation location = locationMaybe.get();
		
		File configurationFile = location.getFile();
		
		Maybe<RepositoryConfiguration> maybeConfig = new ModeledYamlConfigurationLoader() //
				.virtualEnvironment(virtualEnvironment) //
				.variableResolver(location.getProperties()::get)
				.loadConfig(RepositoryConfiguration.T, configurationFile, false);
		
		if (maybeConfig.isUnsatisfied())
			return maybeConfig;
		
		RepositoryConfiguration repositoryConfiguration = maybeConfig.get();
		
		Reason origination = TemplateReasons.build(RepositoryConfigurationLoaded.T) //
									.assign(RepositoryConfigurationLoaded::setUrl,configurationFile.getAbsolutePath()) //
									.toReason(); //
		
		repositoryConfiguration.setOrigination(origination);

		return Maybe.complete(repositoryConfiguration);
	}

	private RepositoryConfigurationLocatorContext buildLocatorContext() {
		return new RepositoryConfigurationLocatorContext() {
			
			@Override
			public VirtualEnvironment getVirtualEnvironment() {
				return virtualEnvironment;
			}
			
			@Override
			public File getDevelopmentEnvironmentRoot() {
				return developmentEnvironmentRoot;
			}
		};
	}
}