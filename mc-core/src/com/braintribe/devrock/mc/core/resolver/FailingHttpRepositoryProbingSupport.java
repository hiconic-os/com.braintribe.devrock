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
package com.braintribe.devrock.mc.core.resolver;

import com.braintribe.devrock.mc.api.repository.RepositoryProbingSupport;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.changes.RepositoryProbeStatus;
import com.braintribe.model.artifact.changes.RepositoryProbingResult;

/**
 * a {@link RepositoryProbingSupport} that just communicates an error
 * @author dirk
 *
 */
public class FailingHttpRepositoryProbingSupport implements RepositoryProbingSupport {
	
	private String repositoryId;
	private Reason failure;
	
	
	public FailingHttpRepositoryProbingSupport(String repositoryId, Reason failure) {
		super();
		this.repositoryId = repositoryId;
		this.failure = failure;
	}

	@Override
	public RepositoryProbingResult probe() {
		RepositoryProbingResult result = RepositoryProbingResult.create(RepositoryProbeStatus.unprobed, failure, repositoryId, null);
		return result;
	}
	
	@Override
	public String repositoryId() {
		return repositoryId;
	}

	
}
