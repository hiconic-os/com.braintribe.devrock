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
package com.braintribe.devrock.mc.core.wirings.maven.configuration.space;

import com.braintribe.devrock.mc.core.configuration.maven.MavenSettingsCompiler;
import com.braintribe.devrock.mc.core.configuration.maven.MavenSettingsLoader;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.contract.MavenConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

/**
 * an implementation of the {@link RepositoryConfigurationContract} tailored to 'maven' standard
 * @author pit / dirk
 *
 */
@Managed
public class MavenConfigurationSpace implements RepositoryConfigurationContract, MavenConfigurationContract {

	@Import
	private VirtualEnvironmentContract virtualEnvironment;
	
	/**
	 * @return - the 'standard' settings loader, i.e. the one that respects standard 
	 * Maven style locations for the two settings files AND our environment variables
	 */
	@Managed
	private MavenSettingsLoader loader() {
		MavenSettingsLoader bean = new MavenSettingsLoader();
		bean.setVirtualEnvironment(virtualEnvironment.virtualEnvironment());
		return bean;
	}
	
	/**
	 * @return - the settings compiler, i.e. the one the resolves variables
	 * and creates a {@link RepositoryConfiguration}
	 */
	@Managed
	private MavenSettingsCompiler compiler() {		
		MavenSettingsCompiler bean = new MavenSettingsCompiler();
		bean.setVirtualEnvironment(virtualEnvironment.virtualEnvironment());
		bean.setSettingsSupplier( loader());
		return bean;
	}
	
	@Override
	public Maybe<RepositoryConfiguration> repositoryConfiguration() {
		try {
			return Maybe.complete(compiler().get());
		}
		catch (Exception e) {
			return InternalError.from(e, "could not load maven configuration").asMaybe();
		}
	}

}
