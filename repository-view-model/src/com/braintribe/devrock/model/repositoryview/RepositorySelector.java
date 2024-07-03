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

import com.braintribe.devrock.model.repositoryview.selectors.ByNameRegexRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ByNameRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ByTypeRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ConjunctionRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.DisjunctionRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.NegationRepositorySelector;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A <code>RepositorySelector</code> is used to select one or more repositories. The selector sub type decides which
 * repository properties/aspects the selection is based on. For example, the simple {@link ByNameRepositorySelector}
 * selector just selects a single repository by name whereas one can also select multiple repositories via regex using
 * {@link ByNameRegexRepositorySelector}. {@link ByTypeRepositorySelector} implements a type based selection. In
 * addition there are also {@link ConjunctionRepositorySelector}, {@link DisjunctionRepositorySelector} and
 * {@link NegationRepositorySelector} to make this more flexible.
 * <p>
 * <code>RepositorySelector</code>s are {@link ConfigurationEnrichment#getSelector() used} in
 * {@link ConfigurationEnrichment}s to specify the repositories to be enriched.
 *
 * @author michael.lafite
 */
@Abstract
public interface RepositorySelector extends GenericEntity {

	final EntityType<RepositorySelector> T = EntityTypes.T(RepositorySelector.class);

	// no properties
}
