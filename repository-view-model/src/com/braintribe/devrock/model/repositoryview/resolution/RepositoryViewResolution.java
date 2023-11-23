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
package com.braintribe.devrock.model.repositoryview.resolution;

import java.util.List;

import com.braintribe.devrock.model.repositoryview.RepositoryView;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A <code>RepositoryViewResolution</code> represents a resolution of {@link RepositoryView}s.
 *
 * @author michael.lafite
 */
public interface RepositoryViewResolution extends GenericEntity {

	final EntityType<RepositoryViewResolution> T = EntityTypes.T(RepositoryViewResolution.class);

	String solutions = "solutions";
	String terminals = "terminals";

	/**
	 * The complete list of all solutions.
	 */
	List<RepositoryViewSolution> getSolutions();
	void setSolutions(List<RepositoryViewSolution> solutions);

	/**
	 * The list of terminals, i.e. the view(s) the resolution was started with.
	 */
	List<RepositoryViewSolution> getTerminals();
	void setTerminals(List<RepositoryViewSolution> terminals);
}
