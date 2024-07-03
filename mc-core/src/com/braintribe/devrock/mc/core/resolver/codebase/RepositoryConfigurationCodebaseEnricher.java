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
package com.braintribe.devrock.mc.core.resolver.codebase;

import java.util.List;
import java.util.function.Consumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.model.repository.CodebaseRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;

public class RepositoryConfigurationCodebaseEnricher implements Consumer<RepositoryConfiguration> {
	private List<CodebaseRepository> codebaseRepositories;

	@Override
	public void accept(RepositoryConfiguration repositoryConfiguration) {
		repositoryConfiguration.getRepositories().addAll(0, codebaseRepositories);
	}

	@Required
	@Configurable
	public void setCodebaseRepositories(List<CodebaseRepository> codebaseRepositories) {
		this.codebaseRepositories = codebaseRepositories;
	}
}
