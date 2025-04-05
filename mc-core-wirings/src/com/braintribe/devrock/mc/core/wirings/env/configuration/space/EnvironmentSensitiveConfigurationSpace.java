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
package com.braintribe.devrock.mc.core.wirings.env.configuration.space;

import com.braintribe.devrock.mc.api.repository.configuration.RawRepositoryConfiguration;
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
		
		return mavenConfigurationFallback(whyEmpty);
	}

	private Maybe<RepositoryConfiguration> mavenConfigurationFallback(Reason whyEmpty) {
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
	
	@Override
	public Maybe<RawRepositoryConfiguration> rawRepositoryConfiguration() {
		Maybe<RawRepositoryConfiguration> repositoryConfigurationPotential = standardRepositoryConfiguration.rawRepositoryConfiguration();
		
		if (repositoryConfigurationPotential.isSatisfied())
			return repositoryConfigurationPotential;
		
		Reason whyEmpty = repositoryConfigurationPotential.whyUnsatisfied();
		
		if (!(whyEmpty instanceof NoRepositoryConfiguration)) {
			return repositoryConfigurationPotential;
		}
		
		Maybe<RepositoryConfiguration> mavenConfigurationPotential = mavenConfigurationFallback(whyEmpty);
		
		if (mavenConfigurationPotential.isUnsatisfied())
			return mavenConfigurationPotential.whyUnsatisfied().asMaybe();
		
		return Maybe.complete(new RawRepositoryConfiguration(mavenConfigurationPotential.get(), null));
	}
}
