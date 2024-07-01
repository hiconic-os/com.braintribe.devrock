// ============================================================================
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
