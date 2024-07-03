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
