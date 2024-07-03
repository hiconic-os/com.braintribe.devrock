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
package com.braintribe.devrock.mc.core.commons;

import com.braintribe.gm.reason.ReasonOutput;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;

public class McReasonOutput extends ReasonOutput {
	public McReasonOutput() {
		registerFormatter(CompiledDependencyIdentification.T, McOutputs::compiledDependencyIdentification);
		registerFormatter(CompiledDependency.T, McOutputs::compiledDependency);
		registerFormatter(AnalysisDependency.T, McOutputs::analysisDependency);
		registerFormatter(CompiledArtifactIdentification.T, McOutputs::compiledArtifactIdentification);
		registerFormatter(PartIdentification.T, McOutputs::partIdentification);
		registerFormatter(CompiledPartIdentification.T, McOutputs::compiledPartIdentification);
		registerFormatter(VersionedArtifactIdentification.T, McOutputs::versionedArtifactIdentification);
		registerFormatter(ArtifactIdentification.T, McOutputs::artifactIdentification);
		registerFormatter(AnalysisArtifact.T, McOutputs::analysisArtifact);
		registerFormatter(VersionExpression.T, McOutputs::versionExpression);
		registerFormatter(Version.T, McOutputs::version);
	}
}
