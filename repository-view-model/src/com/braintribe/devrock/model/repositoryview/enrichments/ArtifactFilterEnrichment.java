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
package com.braintribe.devrock.model.repositoryview.enrichments;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repositoryview.ConfigurationEnrichment;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A {@link ConfigurationEnrichment} which holds only an {@link #getArtifactFilter() artifact filter} and thus only
 * enriches the {@link Repository#getArtifactFilter() artifact filter part} of a {@link Repository}.
 *
 * @author michael.lafite
 */
public interface ArtifactFilterEnrichment extends ConfigurationEnrichment {

	final EntityType<ArtifactFilterEnrichment> T = EntityTypes.T(ArtifactFilterEnrichment.class);

	String artifactFilter = "artifactFilter";

	/**
	 * The filter to be merged into the respective {@link Repository}.
	 */
	ArtifactFilter getArtifactFilter();
	void setArtifactFilter(ArtifactFilter artifactFilter);
}
