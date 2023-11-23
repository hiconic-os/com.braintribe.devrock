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
