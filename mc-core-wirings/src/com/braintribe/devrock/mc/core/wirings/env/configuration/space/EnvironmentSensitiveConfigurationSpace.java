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
package com.braintribe.devrock.mc.core.wirings.env.configuration.space;

import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.StandardRepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.contract.MavenConfigurationContract;
import com.braintribe.devrock.model.mc.reason.InvalidRepositoryConfiguration;
import com.braintribe.devrock.model.mc.reason.NoRepositoryConfiguration;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

/**
 * an implementation of the {@link RepositoryConfigurationContract} tailored to 'maven' standard
 * @author pit / dirk
 *
 */
@Managed
public class EnvironmentSensitiveConfigurationSpace implements RepositoryConfigurationContract {

	@Import
	private StandardRepositoryConfigurationContract standardRepositoryConfiguration;
	
	@Import
	private MavenConfigurationContract mavenConfiguration;
	
	@Override
	public Maybe<RepositoryConfiguration> repositoryConfiguration() {
		Maybe<RepositoryConfiguration> repositoryConfigurationPotential = standardRepositoryConfiguration.repositoryConfiguration();
		
		if (repositoryConfigurationPotential.isSatisfied())
			return repositoryConfigurationPotential;
		
		Reason whyEmpty = repositoryConfigurationPotential.whyUnsatisfied();
		
		if (!(whyEmpty instanceof NoRepositoryConfiguration)) {
			return repositoryConfigurationPotential;
		}
		
		Maybe<RepositoryConfiguration> mavenConfigurationPotential = mavenConfiguration.repositoryConfiguration();
		
		if (mavenConfigurationPotential.isSatisfied())
			return mavenConfigurationPotential;
		
		Reason whyMavenUnsatisfied = mavenConfigurationPotential.whyUnsatisfied();
		
		if (whyMavenUnsatisfied instanceof NoRepositoryConfiguration) {
			return Reasons.build(NoRepositoryConfiguration.T) //
					.text("could not find any configuration") //
					.causes(whyEmpty, mavenConfigurationPotential.whyUnsatisfied()) //
					.toMaybe();
		}
		else {
			return Reasons.build(InvalidRepositoryConfiguration.T) //
					.text("could not retrieve a valid repository configuration in order") //
					.causes(whyEmpty, mavenConfigurationPotential.whyUnsatisfied()) //
					.toMaybe();
		}
		
		
	}

}
