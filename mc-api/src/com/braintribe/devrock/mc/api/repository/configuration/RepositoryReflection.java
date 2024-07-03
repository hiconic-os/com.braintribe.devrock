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
package com.braintribe.devrock.mc.api.repository.configuration;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;

/**
 * reflects the *currently* active configuration of the mc-core,
 * i.e. the 'post-probing and post-enrichment' state
 * @author pit / dirk 
 *
 */
public interface RepositoryReflection {
	/**
	 * @return
	 */
	RepositoryViewResolution getRepositoryViewResolution();
	
	/**
	 * @return - the currently active (probed and enriched) {@link RepositoryConfiguration}
	 */
	RepositoryConfiguration getRepositoryConfiguration();
	
	/**
	 * @param repoName - the name of the {@link Repository} to look-up
	 * @return - the {@link Repository} or null if not found
	 */
	Repository getRepository(String repoName);
	
	/**
	 * @return - the repository declared as 'standard upload' repository 
	 */
	Repository getUploadRepository();
	
	/**
	 * @param repoName - the name of the {@link Repository} to look-up
	 * @return - true if the repository reflects a source repository (git checkout)
	 */
	boolean isCodebase(String repoName);
}
