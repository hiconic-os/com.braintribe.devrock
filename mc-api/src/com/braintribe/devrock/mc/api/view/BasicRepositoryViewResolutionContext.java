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
package com.braintribe.devrock.mc.api.view;

import java.util.Collections;
import java.util.List;

import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repositoryview.ConfigurationEnrichment;

public class BasicRepositoryViewResolutionContext implements RepositoryViewResolutionContext, RepositoryViewResolutionContextBuilder {
	private List<ConfigurationEnrichment> enrichments = Collections.emptyList();
	private RepositoryConfiguration baseConfiguration;

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
}
