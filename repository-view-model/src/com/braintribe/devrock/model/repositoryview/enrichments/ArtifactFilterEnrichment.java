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
