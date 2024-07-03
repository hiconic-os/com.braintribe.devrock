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
package com.braintribe.devrock.model.repository;

import java.nio.file.Paths;
import java.util.List;

import com.braintribe.gm.model.reason.HasFailure;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents the configuration about repositories (and their filter), plus the local repo
 * @author pit
 *
 */
public interface RepositoryConfiguration extends HasFailure {
	
	EntityType<RepositoryConfiguration> T = EntityTypes.T(RepositoryConfiguration.class);

	String repositories = "repositories";
	String cachePath = "cachePath";
	String localRepositoryPath = "localRepositoryPath";
	String offline = "offline";
	String uploadRepository = "uploadRepository";
	String installRepository = "installRepository";

	/** optional name for better identification by a human */
	String getName();
	void setName(String name);

	// TODO: remove when no longer needed
	Repository getUploadRepository();
	void setUploadRepository(Repository uploadRepository);
	
	Repository getInstallRepository();
	void setInstallRepository(Repository installRepository);

	/**
	 * @return - a {@link List} of {@link Repository} that are relevant
	 */
	List<Repository> getRepositories();
	void setRepositories(List<Repository> value);
	/**
	 * @return - the path to the local repository's filesystem
	 * @deprecated use {@link #getCachePath()} and {@link #getInstallRepository()} instead.
	 * @see RepositoryConfiguration#getCachePath() 
	 */
	@Deprecated
	String getLocalRepositoryPath();
	@Deprecated
	void setLocalRepositoryPath(String value);
	
	/**
	 * Configures the file path where the resolver will download parts from cachable repos to. 
	 * In contrast to the {@link #getLocalRepositoryPath() localRepositoryPath} the cache will never act as a repository itself
	 * thus following a proper SOC design. 
	 * 
	 * <p>
	 * If not null the {@link #getLocalRepositoryPath() localRespositoryPath} will be ignored in favor of the cache path. 
	 *   
	 * @return
	 */
	String getCachePath();
	void setCachePath(String cachePath);
	
	/**
	 * @return - true if the configuration (all repositories contained) are to be treated to be offline
	 */
	boolean getOffline();
	void setOffline(boolean offlineStatus);

	/**
	 * @return - a reason (with potentially more attached) that reflect how this instance came into being
	 */
	Reason getOrigination();
	void setOrigination(Reason value);
	
	default String cachePath() {
		String path = getCachePath();
		if (path == null) {
			path = getLocalRepositoryPath();
		}
		return Paths.get(path).normalize().toString();
	}

}
