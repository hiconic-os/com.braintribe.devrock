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
package com.braintribe.devrock.mc.core.configuration;

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocaterBuilder;
import com.braintribe.utils.paths.UniversalPath;

public interface RepositoryConfigurationLocators {
	public static final String FOLDERNAME_ARTIFACTS = "artifacts";
	public static final String FOLDERNAME_DEVROCK = ".devrock";
	public static final String ENV_DEVROCK_REPOSITORY_CONFIGURATION = "DEVROCK_REPOSITORY_CONFIGURATION";
	public static final String FILENAME_REPOSITORY_CONFIGURATION = "repository-configuration.yaml";

	
	static RepositoryConfigurationLocaterBuilder build() {
		return new RepositoryConfigurationLocatorBuilderImpl();
	};
	
	static RepositoryConfigurationLocaterBuilder buildDefault() {
		// devenv, env, userhome
		return new RepositoryConfigurationLocatorBuilderImpl() //
				.addDevEnvLocation(UniversalPath.start(FOLDERNAME_ARTIFACTS).push(FILENAME_REPOSITORY_CONFIGURATION)) //
				.addLocationEnvVariable(ENV_DEVROCK_REPOSITORY_CONFIGURATION) //
				.addUserDirLocation(UniversalPath.start(FOLDERNAME_DEVROCK).push(FILENAME_REPOSITORY_CONFIGURATION));
	};
	
	
}
