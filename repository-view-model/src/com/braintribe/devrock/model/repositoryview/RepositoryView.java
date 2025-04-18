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
package com.braintribe.devrock.model.repositoryview;

import java.util.List;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.LockArtifactFilter;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * <code>RepositoryView</code> holds (partial or full) {@link Repository} data based on which a
 * {@link RepositoryConfiguration} can be created. As single <code>RepositoryView</code> may hold all required data, but
 * multiple views can also be combined.<br>
 * The <code>RepositoryView</code> is called <i>view</i> because the initial purpose was to transport
 * {@link ArtifactFilter}s which filter access on a repository and thus create a <i>view</i> on the repository, similar
 * to a view in a database.
 * <p>
 * A <code>RepositoryView</code> may declare one or multiple {@link #getRepositories() repositories} on its own and/or
 * provide data to {@link #getEnrichments() enrich} repositories (declared in other views).
 * <p>
 * Views which represent a release may also have an (optional) {@link #getRelease() release} reference which provides
 * further information about the respective release.
 *
 * @author michael.lafite
 */
public interface RepositoryView extends GenericEntity {

	EntityType<RepositoryView> T = EntityTypes.T(RepositoryView.class);

	String displayName = "displayName";
	String enrichments = "enrichments";
	String immutable = "immutable";
	String release = "release";
	String repositories = "repositories";

	/**
	 * An (optional) display name. If set, the display name will be shown instead of the artifact name in certain places
	 * (such as logs or the landing page).
	 */
	String getDisplayName();
	void setDisplayName(String displayName);

	/**
	 * The (optional) list of {@link ConfigurationEnrichment}s used to enrich repositories (usually added by other
	 * views).
	 */
	List<ConfigurationEnrichment> getEnrichments();
	void setEnrichments(List<ConfigurationEnrichment> enrichments);

	/**
	 * A flag which indicates whether the content provided by this view is (supposed to be) immutable. Immutability
	 * means that for artifacts matched by {@link ArtifactFilter}s only concrete versions are matched (using
	 * {@link LockArtifactFilter}. Also the view's<code>dependencies</code> are fixed, i.e. no ranges. If the view has a
	 * <code>parent</code> artifact, it's a fixed version too.<br>
	 * In the future there will be automated checks to ensure this, e.g. when the artifact is installed/published.
	 * <p>
	 * Usually this flag is set for {@link #getRelease() release} views, but it's technically not limited to those.
	 */
	boolean getImmutable();
	void setImmutable(boolean immutable);

	/**
	 * An (optional) {@link Release} reference which indicates that this view represents a release and which may also
	 * provide further information about that release.
	 */
	Release getRelease();
	void setRelease(Release release);

	/**
	 * The (optional) list of {@link Repository}s to be added to the {@link RepositoryConfiguration}. Instead of
	 * specifying repositories on its own a view may also {@link #getEnrichments() enrich} repositories added by other
	 * views.
	 */
	List<Repository> getRepositories();
	void setRepositories(List<Repository> repositories);
}
