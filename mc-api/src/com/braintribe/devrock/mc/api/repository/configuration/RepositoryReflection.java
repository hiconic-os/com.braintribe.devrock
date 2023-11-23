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
