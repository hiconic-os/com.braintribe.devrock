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
package com.braintribe.devrock.mc.core.wirings.configuration.contract;

import com.braintribe.devrock.mc.api.repository.configuration.RawRepositoryConfiguration;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.wire.api.space.WireSpace;

/**
 * the contract for {@link RepositoryConfiguration} providers
 * @author pit / dirk
 *
 */
public interface RepositoryConfigurationContract extends WireSpace {
	/**
	 * @return - a {@link RepositoryConfiguration}
	 */
	Maybe<RepositoryConfiguration> repositoryConfiguration();

	/**
	 * This method serves to expose the repository configuration before
	 * evaluation of the property placeholders if the supplier supports such
	 * a thing. The default implementation will simply return the repository configuration.
	 */
	default Maybe<RawRepositoryConfiguration> rawRepositoryConfiguration() {
		var maybe = repositoryConfiguration();
		if (maybe.isUnsatisfied())
			return maybe.whyUnsatisfied().asMaybe();
		
		return Maybe.complete(new RawRepositoryConfiguration(maybe.get(), null));
	}
}
