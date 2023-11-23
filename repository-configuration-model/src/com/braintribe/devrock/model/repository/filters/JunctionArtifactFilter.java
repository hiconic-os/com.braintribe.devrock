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
