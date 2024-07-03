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

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * ATTENTION: This filter is just experimental. It may be changed or removed anytime.<br>
 * An {@link ArtifactFilter} that is the typically used in so-called development views, i.e. during development. It uses
 * a delegate {@link #getRestrictionFilter() restrictionFilter} to only restrict specified groups/artifacts but
 * otherwise matches everything else. This means that one can e.g. add a filter for tribefire and a few extensions.
 * Access to the respective groups/artifact/versions will be filtered as usual. Anything else will be fully matched
 * though, i.e. not restricted at all. This enables developers to e.g. add another extension or a third party library.
 *
 * @author michael.lafite
 */
public interface StandardDevelopmentViewArtifactFilter extends ArtifactFilter {

	EntityType<StandardDevelopmentViewArtifactFilter> T = EntityTypes.T(StandardDevelopmentViewArtifactFilter.class);

	String restrictionFilter = "restrictionFilter";
	String restrictOnArtifactInsteadOfGroupLevel = "restrictOnArtifactInsteadOfGroupLevel";

	/**
	 * A delegate filter used to restrict access to certain groups/artifacts/versions.
	 */
	ArtifactFilter getRestrictionFilter();
	void setRestrictionFilter(ArtifactFilter restrictionFilter);

	/**
	 * If the delegate {@link #getRestrictionFilter() restriction filter} matches a group, it normally becomes fully
	 * responsible for that group, meaning that only artifacts matched by the delegate filter will be matched. Enabling
	 * this property switches to an alternative mode where the delegate filter is only responsible for the artifacts it
	 * matches. Access to all other artifacts will not be restricted at all.
	 */
	@Initializer("false")
	Boolean getRestrictOnArtifactInsteadOfGroupLevel();
	void setRestrictOnArtifactInsteadOfGroupLevel(Boolean restrictOnArtifactInsteadOfGroupLevel);
}
