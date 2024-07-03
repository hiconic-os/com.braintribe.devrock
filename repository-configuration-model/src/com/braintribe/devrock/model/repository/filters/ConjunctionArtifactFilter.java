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
 * An {@link ArtifactFilter} that can be used to create a conjunction of other {@link ArtifactFilter}s. All delegate
 * filter {@link #getOperands() operands} must match so that this filter matches.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public interface ConjunctionArtifactFilter extends JunctionArtifactFilter {

	EntityType<ConjunctionArtifactFilter> T = EntityTypes.T(ConjunctionArtifactFilter.class);
}
