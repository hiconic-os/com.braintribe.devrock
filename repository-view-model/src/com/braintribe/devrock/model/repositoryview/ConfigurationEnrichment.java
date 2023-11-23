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
