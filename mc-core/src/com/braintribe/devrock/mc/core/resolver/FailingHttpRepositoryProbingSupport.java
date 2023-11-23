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
