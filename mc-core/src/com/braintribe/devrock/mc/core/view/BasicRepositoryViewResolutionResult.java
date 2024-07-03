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
