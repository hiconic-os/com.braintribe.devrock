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
package com.braintribe.devrock.mc.api.view;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repositoryview.ConfigurationEnrichment;

public class BasicRepositoryViewResolutionContext implements RepositoryViewResolutionContext, RepositoryViewResolutionContextBuilder {
	private List<ConfigurationEnrichment> enrichments = Collections.emptyList();
	private RepositoryConfiguration baseConfiguration;
	private File effectiveRepoConfigFolder;
	private String viewConfigName = "repository-configuration";

	@Override
	public RepositoryViewResolutionContext done() {
		return this;
	}

	@Override
	public RepositoryViewResolutionContextBuilder enrich(List<ConfigurationEnrichment> enrichments) {
		this.enrichments = enrichments;
		return this;
	}

	@Override
	public RepositoryViewResolutionContextBuilder baseConfiguration(RepositoryConfiguration baseConfiguration) {
		this.baseConfiguration = baseConfiguration;
		return this;
	}

	@Override
	public List<ConfigurationEnrichment> enrichments() {
		return enrichments;
	}

	@Override
	public RepositoryConfiguration baseConfiguration() {
		return baseConfiguration;
	}
	
	@Override
	public File effectiveRepoConfigFolder() {
		return effectiveRepoConfigFolder;
	}
	
	@Override
	public RepositoryViewResolutionContextBuilder effectiveRepoConfigFolder(File effectiveRepoConfigFolder) {
		this.effectiveRepoConfigFolder = effectiveRepoConfigFolder;
		return this;
	}
	
	@Override
	public String viewConfigName() {
		return viewConfigName;
	} 
	
	@Override
	public RepositoryViewResolutionContextBuilder viewConfigName(String viewConfigName) {
		this.viewConfigName = viewConfigName;
		return this;
	}
}
