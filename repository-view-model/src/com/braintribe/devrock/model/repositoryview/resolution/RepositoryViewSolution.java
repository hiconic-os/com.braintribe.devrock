// ============================================================================
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
 * A <code>RepositoryViewSolution</code> represents a single solution in a {@link RepositoryViewResolution}.
 *
 * @author michael.lafite
 */
public interface RepositoryViewSolution extends GenericEntity {

	final EntityType<RepositoryViewSolution> T = EntityTypes.T(RepositoryViewSolution.class);

	String artifact = "artifact";
	String repositoryView = "repositoryView";
	String dependencies = "dependencies";

	/**
	 * The (fully qualified) artifact that holds the {@link #getRepositoryView() repository view}, e.g.
	 * <code>org.example:example-release-view#1.2.3</code>.
	 */
	String getArtifact();
	void setArtifact(String artifact);

	/**
	 * The {@link RepositoryView} attached to the {@link #getArtifact() artifact}.
	 */
	RepositoryView getRepositoryView();
	void setRepositoryView(RepositoryView repositoryView);

	/**
	 * The list of dependencies of this {@link #getRepositoryView() repository view}, i.e. actually the dependencies in
	 * the respective {@link #getArtifact() artifact}'s POM.
	 */
	List<RepositoryViewSolution> getDependencies();
	void setDependencies(List<RepositoryViewSolution> dependencies);
}
