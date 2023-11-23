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
package com.braintribe.devrock.mc.core.view;

import com.braintribe.devrock.mc.api.view.RepositoryViewResolutionResult;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class BasicRepositoryViewResolutionResult implements RepositoryViewResolutionResult {

	private RepositoryConfiguration mergedRepositoryConfiguration;
	private RepositoryViewResolution viewResolution;
	private AnalysisArtifactResolution analysisResolution;

	public BasicRepositoryViewResolutionResult(AnalysisArtifactResolution analysisResolution,
			RepositoryViewResolution viewResolution, RepositoryConfiguration mergedRepositoryConfiguration) {
		super();
		this.analysisResolution = analysisResolution;
		this.viewResolution = viewResolution;
		this.mergedRepositoryConfiguration = mergedRepositoryConfiguration;
	}

	@Override
	public AnalysisArtifactResolution getAnalysisResolution() {
		return analysisResolution;
	}

	@Override
	public RepositoryViewResolution getRepositoryViewResolution() {
		return viewResolution;
	}

	@Override
	public RepositoryConfiguration getMergedRepositoryConfiguration() {
		return mergedRepositoryConfiguration;
	}

}
