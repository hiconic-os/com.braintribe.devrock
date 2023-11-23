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
package com.braintribe.devrock.model.repositoryview;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A <code>Release</code> can be {@link RepositoryView#getRelease() attached} to a {@link RepositoryView} to indicate
 * that the respective view represents a release and to provide further information about that release. For now this
 * entity doesn't have any properties yet, but in the future we might want to add information such as (links to) release
 * notes. Therefore the release is modeled as a separate entity and not just an <code>isRelease</code> property on the
 * view.
 *
 * @author michael.lafite
 */
public interface Release extends GenericEntity {

	final EntityType<Release> T = EntityTypes.T(Release.class);

	// no properties
}
