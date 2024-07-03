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

import java.util.List;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Abstract super type for {@link ConjunctionArtifactFilter} and {@link DisjunctionArtifactFilter} which both have a
 * list of {@link ArtifactFilter} {@link #getOperands() operands}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
@Abstract
public interface JunctionArtifactFilter extends ArtifactFilter {

	EntityType<JunctionArtifactFilter> T = EntityTypes.T(JunctionArtifactFilter.class);

	String operands = "operands";

	/**
	 * The delegate filters.
	 */
	List<ArtifactFilter> getOperands();
	void setOperands(List<ArtifactFilter> operands);
}
