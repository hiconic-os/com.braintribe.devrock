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
