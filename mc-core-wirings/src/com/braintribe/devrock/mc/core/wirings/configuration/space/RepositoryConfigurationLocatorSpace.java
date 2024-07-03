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

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocator;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLocators;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationLocatorContract;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class RepositoryConfigurationLocatorSpace implements RepositoryConfigurationLocatorContract {

	@Managed
	@Override
	public RepositoryConfigurationLocator repositoryConfigurationLocator() {
		return RepositoryConfigurationLocators.buildDefault().done();
	}
}
