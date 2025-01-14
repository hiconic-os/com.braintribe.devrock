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
package devrock.releasing.model.configuration;

import java.util.List;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
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
	@Mandatory
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
