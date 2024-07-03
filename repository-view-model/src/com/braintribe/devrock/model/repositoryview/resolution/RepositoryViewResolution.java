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
