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
package devrock.releasing.model.configuration;

import java.util.List;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Represents a configuration (for a release) based on which a RepositoryView (from repository-view-model) is created.
 * 
 * <h2>About releasing</h2>
 * 
 * A release is implemented as a repository view, which is a type of repository configuration. It consists of one or more Repository instances, each
 * with a filter that only makes the desired groups and artifacts (of a specific version) visible. Such a repository view is represented by an
 * artifact, which is marked with a <code>release-view: true</code> property in its <code>pom.xml</code> .
 * <p>
 * Each release view artifact has a <code>release-configuration.yaml</code> file, obviously containing an instance of this type. Installing such an
 * artifact means resolving all the artifacts with their latest versions from the configured repository and creating an {@link ArtifactFilter} based
 * on {@link #getIncludes()} and {@link #getExcludes()}.
 */
public interface ReleaseConfiguration extends GenericEntity {

	EntityType<ReleaseConfiguration> T = EntityTypes.T(ReleaseConfiguration.class);

	/**
	 * Prototype for the repository of the final RepositoryView, whose {@link Repository#getArtifactFilter() filter} will be computed based on
	 * {@link #getIncludes()} and {@link #getExcludes()}.
	 */
	Repository getRepositoryPrototype();
	void setRepositoryPrototype(Repository repositoryPrototype);

	/**
	 * TODO
	 * <ul>
	 * <li>some.group-id
	 * <li>some.group-id:artifact-id
	 * <li>some.group.*
	 * <li>some.group.*:*-model
	 * </ul>
	 * 
	 */
	List<String> getIncludes();
	void setIncludes(List<String> includes);

	/** Same syntax as {@link #getIncludes()} */
	List<String> getExcludes();
	void setExcludes(List<String> excludes);

}
