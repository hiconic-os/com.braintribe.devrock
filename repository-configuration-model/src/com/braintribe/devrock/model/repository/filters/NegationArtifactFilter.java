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
 * An {@link ArtifactFilter} that creates the negation of its {@link #getOperand() delegate filter}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public interface NegationArtifactFilter extends ArtifactFilter {

	EntityType<NegationArtifactFilter> T = EntityTypes.T(NegationArtifactFilter.class);

	String operand = "operand";

	/**
	 * The filter to negate, i.e. if the delegate matches, this filter does not match (and vice-versa).
	 */
	ArtifactFilter getOperand();
	void setOperand(ArtifactFilter operand);
}
