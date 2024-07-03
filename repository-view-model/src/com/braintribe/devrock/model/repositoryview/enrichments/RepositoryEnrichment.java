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
package com.braintribe.devrock.model.repositoryview.enrichments;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repositoryview.ConfigurationEnrichment;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A {@link ConfigurationEnrichment} which holds only a {@link #getRepository() repository} and may thus enrich any
 * {@link Repository} data.
 *
 * @author michael.lafite
 */
public interface RepositoryEnrichment extends ConfigurationEnrichment {

	final EntityType<RepositoryEnrichment> T = EntityTypes.T(RepositoryEnrichment.class);

	String repository = "repository";

	/**
	 * The repository whose data to merge into the respective target repository. Any non-absent property (except the
	 * {@link Repository#getName() name} will used to enrich the target.
	 */
	Repository getRepository();
	void setRepository(Repository repository);
}
