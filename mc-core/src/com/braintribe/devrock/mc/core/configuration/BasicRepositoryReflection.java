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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.repository.CodebaseRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.WorkspaceRepository;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;

public class BasicRepositoryReflection implements RepositoryReflection {
	private RepositoryConfiguration repositoryConfiguration;
	private final Map<String, Repository> repositoryByName = new HashMap<>();
	private Supplier<RepositoryViewResolution> repositoryViewResolutionSupplier;

	@Required @Configurable
	public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
		this.repositoryConfiguration = repositoryConfiguration;
		this.repositoryConfiguration.getRepositories().forEach(r -> repositoryByName.put(r.getName(), r));
	}
	
	@Configurable
	public void setRepositoryViewResolutionSupplier(
			Supplier<RepositoryViewResolution> repositoryViewResolutionSupplier) {
		this.repositoryViewResolutionSupplier = repositoryViewResolutionSupplier;
	}

	@Override
	public RepositoryConfiguration getRepositoryConfiguration() {
		return repositoryConfiguration;
	}

	@Override
	public Repository getRepository(String repoName) {
		return repositoryByName.get(repoName);
	}

	@Override
	public Repository getUploadRepository() {
		return repositoryConfiguration.getUploadRepository();
	}
	
	@Override
	public boolean isCodebase(String repoName) {
		Repository repository = getRepository(repoName);
		return repository instanceof CodebaseRepository || repository instanceof WorkspaceRepository;
	}
	
	@Override
	public RepositoryViewResolution getRepositoryViewResolution() {
		return repositoryViewResolutionSupplier != null? //
				repositoryViewResolutionSupplier.get(): //
				null;
	}
}
