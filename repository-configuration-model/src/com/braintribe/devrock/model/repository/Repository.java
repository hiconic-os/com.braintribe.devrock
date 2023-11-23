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
package com.braintribe.devrock.model.repository;

import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.gm.model.reason.HasFailure;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.time.TimeSpan;

/**
 * a dumbed- down/simplified version of a repository, containing only the minimal requirements, but still allowing all
 * sensible configuration possibilities
 *
 * @author pit
 *
 */
// this type is intentionally not abstract, since repository views may use it to hold (partial) repository data
public interface Repository extends HasFailure {

	final EntityType<Repository> T = EntityTypes.T(Repository.class);

	String restSupport = "restSupport";
	String name = "name";
	String offline = "offline";
	String artifactFilter = "artifactFilter";
	String dominanceFilter = "dominanceFilter";
	String updateTimespan = "updateTimespan";
	
	String changesUrl = "changesUrl";
	String changesIndexType = "changesIndexType";
	
	String snapshotRepo = "snapshotRepo";
	String cachable = "cachable";

	/**
	 * @return - the {@link RepositoryRestSupport} of the {@link Repository}
	 */
	RepositoryRestSupport getRestSupport();
	void setRestSupport(RepositoryRestSupport value);
	
	/**
	 * @return - the name (and ID) of the {@link Repository}
	 */
	@Mandatory
	String getName();
	void setName(String name);

	
	/**
	 * @return - the {@link ArtifactFilter}, i.e. a filter on the artifact (group or release/snapshot)
	 */
	ArtifactFilter getArtifactFilter();
	void setArtifactFilter(ArtifactFilter ArtifactFilter);

	/**
	 * @return - the {@link ArtifactFilter} that can tell whether the repo is dominant for a certain
	 *         ArtifactIdentification
	 */
	ArtifactFilter getDominanceFilter();
	void setDominanceFilter(ArtifactFilter value);

	/**
	 * @return - true if the repository is offline
	 */
	boolean getOffline();
	void setOffline(boolean offline);

	/**
	 * @return - true if the repo influences the cache when being used in a local repository based resolver
	 */
	@Initializer("true")
	boolean getCachable();
	void setCachable(boolean cachable);
	
	

	/**
	 * @return - the url used to track changes (aka ravenhurst url)
	 */
	String getChangesUrl();
	void setChangesUrl(String changesUrl);
	
	@Initializer("incremental")
	ChangesIndexType getChangesIndexType();
	void setChangesIndexType(ChangesIndexType changesIndexType);

	/**
	 * translates how a 'null' value in the property 'updateTimeSpan' is interpreted.. if true, 'null' is turned into 'daily' aka 1 day, if false it remains 'null' 
	 * @return - true if the repository is supposed be updated.. 
	 */
	@Initializer( "true")
	boolean getUpdateable();
	void setUpdateable( boolean updateable);
	
	/**
	 * null -> never, 0 -> always, others expressed in the value
	 *
	 * @return - the {@link TimeSpan} after the metadata is considered stale
	 */
	TimeSpan getUpdateTimeSpan();
	void setUpdateTimeSpan(TimeSpan updateTimespan);
	
	/**
	 * @return - true if it's handling SNAPSHOTs
	 */	
	boolean getSnapshotRepo();	
	void setSnapshotRepo(boolean snapshotRepo);

}
