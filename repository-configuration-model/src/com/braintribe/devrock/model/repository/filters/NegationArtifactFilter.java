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
