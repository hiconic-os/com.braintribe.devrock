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
