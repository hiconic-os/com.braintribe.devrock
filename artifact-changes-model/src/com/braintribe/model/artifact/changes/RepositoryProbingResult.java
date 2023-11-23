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
package com.braintribe.model.artifact.changes;

import java.util.Date;

import com.braintribe.devrock.model.repository.RepositoryRestSupport;
import com.braintribe.gm.model.reason.HasFailure;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * modeled representation of the {@link RepositoryProbingResult} as returned by the repository probers
 * 
 * @author pit
 *
 */
public interface RepositoryProbingResult extends HasFailure {
	
	EntityType<RepositoryProbingResult> T = EntityTypes.T(RepositoryProbingResult.class);

	String repositoryProbeStatus = "repositoryProbeStatus";
	String changesUrl = "changesUrl";
	String repositoryRestSupport = "repositoryRestSupport";
	String timestamp = "timestamp";
	
	/**
	 * @return - {@link RepositoryProbeStatus} as determined by the prober 
	 */
	RepositoryProbeStatus getRepositoryProbeStatus();
	void setRepositoryProbeStatus(RepositoryProbeStatus value);
	
	/**
	 * @return - the changes URL the repository sent
	 */
	String getChangesUrl();
	void setChangesUrl(String value);

	/**
	 * @return - the level of {@link RepositoryRestSupport} supported by the repo
	 */
	RepositoryRestSupport getRepositoryRestSupport();
	void setRepositoryRestSupport(RepositoryRestSupport value);

	/**
	 * @return - the date as stored (just for info)
	 */
	Date getTimestamp();
	void setTimestamp(Date value);


	/**
	 * creates and parameterizes a new {@link RepositoryProbingResult}, timestamp is now
	 * @param failure - optional failure
	 * @param changesUrl - the {@link String} with the URL for changes 
	 * @param restSupport - the {@link RepositoryRestSupport}
	 * @return - a newly instantiated {@link RepositoryProbingResult}
	 */
	static RepositoryProbingResult create( RepositoryProbeStatus status, Reason failure, String changesUrl, RepositoryRestSupport restSupport) {		
		return create( status, failure, changesUrl, restSupport, new Date());
	}
	
	/**
	 * creates and parameterizes a new {@link RepositoryProbingResult}
	 * @param failure - optional failure
	 * @param changesUrl - the {@link String} with the URL for changes 
	 * @param restSupport - the {@link RepositoryRestSupport}
	 * @param timestamp - the timestamp of the result
	 * @return - a newly instantiated {@link RepositoryProbingResult}
	 */
	static RepositoryProbingResult create( RepositoryProbeStatus status, Reason failure, String changesUrl, RepositoryRestSupport restSupport, Date timestamp) {
		RepositoryProbingResult result = RepositoryProbingResult.T.create();
		result.setRepositoryProbeStatus(status);
		result.setFailure(failure);
		result.setChangesUrl(changesUrl);
		result.setRepositoryRestSupport(restSupport);
		result.setTimestamp(timestamp);
		return result;
	}
}
