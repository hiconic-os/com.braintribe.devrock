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

import java.io.File;
import java.util.List;
import java.util.Map;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * the RH handler
 * @author pit / dirk 
 *
 */
public interface ArtifactChangesSynchronization {
	
	/**
	 * query RH for the contents (i.e. call without timestamp)
	 * @param repository - the {@link Repository} to query about 
	 * @return - a {@link Maybe} link List} of {@link VersionedArtifactIdentification} : the artifacts contained in the repository
	 */
	Maybe<List<VersionedArtifactIdentification>> queryContents( Repository repository);
	
	/**
	 * query RH for changes 
	 * @param localRepo - the root folder of the local repository 
	 * @param repository - the {@link Repository} to query about 
	 * @return - a {@link List} of {@link VersionedArtifactIdentification} : the changes artifacts
	 */
	List<VersionedArtifactIdentification> queryChanges( File localRepo, Repository repository);
	
	/**
	 * reflect the RH changes on the index files in the local repository (maven-metadata, part-availability)
	 * @param localRepo - the root folder of the local repository 
	 * @param vais - the {@link List} of {@link VersionedArtifactIdentification}: the changed artifacts
	 */
	void purge( File localRepo, Map<Repository, List<VersionedArtifactIdentification>> vais);

	/**
	 * returns the filter for repository  
	 * @param localRepo
	 * @param repository
	 * @return
	 */
	ArtifactFilter getFilterForRepository(File localRepo, Repository repository);
	
	

}
