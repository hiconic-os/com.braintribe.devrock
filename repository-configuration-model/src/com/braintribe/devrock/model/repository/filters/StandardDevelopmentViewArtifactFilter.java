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
