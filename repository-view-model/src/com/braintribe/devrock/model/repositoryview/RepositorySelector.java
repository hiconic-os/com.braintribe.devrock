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
