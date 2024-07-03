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

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repositoryview.enrichments.ArtifactFilterEnrichment;
import com.braintribe.devrock.model.repositoryview.enrichments.RepositoryEnrichment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A <code>ConfigurationEnrichment</code> is used to enrich configuration settings in a {@link Repository}. Which part
 * of the configuration is enriched depends on the enrichment subtype. The {@link RepositoryEnrichment} holds a
 * {@link RepositoryEnrichment#getRepository() repository} and can thus enrich any repository settings. (Any non-absent
 * property will be used to enrich the target repository.) In contrast, the {@link ArtifactFilterEnrichment} holds only
 * an {@link ArtifactFilterEnrichment#getArtifactFilter() artifact filter}. This enrichment type can be used to specify
 * only a filter. Although the same can be achieved via {@link RepositoryEnrichment}, this is more expressive / less
 * bloated.<br>
 * Further <code>ConfigurationEnrichment</code> sub types may be added in the future, e.g. for credentials.
 * <p>
 * A <code>ConfigurationEnrichment</code> has a {@link #getSelector() selector} which is used to select the repositories
 * to be enriched.
 *
 * @author michael.lafite
 */
@Abstract
public interface ConfigurationEnrichment extends GenericEntity {

	final EntityType<ConfigurationEnrichment> T = EntityTypes.T(ConfigurationEnrichment.class);

	String selector = "selector";

	/**
	 * The selector used to select the repositories to be enriched.
	 */
	RepositorySelector getSelector();
	void setSelector(RepositorySelector selector);
}
