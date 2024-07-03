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
package com.braintribe.devrock.mc.core.compiled;

import java.util.List;
import java.util.Set;

import com.braintribe.model.artifact.declared.DeclaredDependency;
import com.braintribe.model.artifact.declared.ProcessingInstruction;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

public interface ReasonedDeclaredDependency {
	
	ReasonedAccessor<DeclaredDependency, String> groupId = ReasonedAccessor.build(DeclaredDependency.T, DeclaredDependency.groupId);
	ReasonedAccessor<DeclaredDependency, String> artifactId = ReasonedAccessor.build(DeclaredDependency.T, DeclaredDependency.artifactId);
	ReasonedAccessor<DeclaredDependency, Set<ArtifactIdentification>> exclusions = ReasonedAccessor.build(DeclaredDependency.T, DeclaredDependency.exclusions);
	ReasonedAccessor<DeclaredDependency, Boolean> optional = ReasonedAccessor.build(DeclaredDependency.T, DeclaredDependency.optional);
	ReasonedAccessor<DeclaredDependency, String> scope = ReasonedAccessor.build(DeclaredDependency.T, DeclaredDependency.scope);
	ReasonedAccessor<DeclaredDependency, String> type = ReasonedAccessor.build(DeclaredDependency.T, DeclaredDependency.type);
	ReasonedAccessor<DeclaredDependency, String> classifier = ReasonedAccessor.build(DeclaredDependency.T, DeclaredDependency.classifier);
	ReasonedAccessor<DeclaredDependency, String> version = ReasonedAccessor.build(DeclaredDependency.T, DeclaredDependency.version);
	ReasonedAccessor<DeclaredDependency, List<ProcessingInstruction>> processingInstructions = ReasonedAccessor.build(DeclaredDependency.T, DeclaredDependency.processingInstructions);
	
}
