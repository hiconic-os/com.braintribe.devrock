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
package com.braintribe.devrock.model.repository.filters;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Placeholder for a filter derived from repository information, such as the artifact index.
 * Repository probing replaces occurrences in the configured filter graph with the derived filter.
 * Outside that context, or when no derived filter is available, it matches everything.
 *
 * An explicit filter without this placeholder is authoritative. To restrict it to the index,
 * combine it with this filter in a {@link ConjunctionArtifactFilter}; to extend the index,
 * use a {@link DisjunctionArtifactFilter}. An unspecified repository filter implicitly uses this filter.
 */
public interface AutoArtifactFilter extends ArtifactFilter {

	EntityType<AutoArtifactFilter> T = EntityTypes.T(AutoArtifactFilter.class);
}
