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
package com.braintribe.devrock.model.repositoryview;

import java.util.List;

import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A {@link RepositoryConfiguration} specifically used to resolve {@link #getViews() views} based on which the "real"
 * <code>RepositoryConfiguration</code> will be created.
 * 
 * @author michael.lafite
 */
public interface ViewRepositoryConfiguration extends RepositoryConfiguration {

	EntityType<ViewRepositoryConfiguration> T = EntityTypes.T(ViewRepositoryConfiguration.class);

	String views = "views";
	String injectedViews = "injectedViews";
	String baseConfiguration = "baseConfiguration";

	// e.g. ['tribefire.release:tribefire-release-view#2.1.7', 'tribefire.simple.extension:simple-release-view#2.2']
	List<String> getViews();
	void setViews(List<String> value);

	/**
	 * The (optional) list of {@link ConfigurationEnrichment}s used to enrich repositories (usually added by other
	 * views).
	 */
	List<ConfigurationEnrichment> getEnrichments();
	void setEnrichments(List<ConfigurationEnrichment> enrichments);

	/**
	 * A base configuration into which the views will be merged. If not set, defaults for global properties (such as
	 * 'offline') will be taken from this {@link ViewRepositoryConfiguration}.
	 */
	RepositoryConfiguration getBaseConfiguration();
	void setBaseConfiguration(RepositoryConfiguration value);
}
