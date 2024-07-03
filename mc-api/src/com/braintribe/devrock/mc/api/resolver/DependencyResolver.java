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
package com.braintribe.devrock.mc.api.resolver;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;

/**
 * the dependency resolver - resolves a {@link CompiledDependencyIdentification} and returns the single BEST MATCHING {@link CompiledArtifactIdentification}
 * @author pit/dirk
 *
 */
public interface DependencyResolver {
	
	/**
	 * @param dependencyIdentification - the {@link CompiledDependencyIdentification}
	 * @return - the best matching {@link CompiledArtifactIdentification} 
	 */
	Maybe<CompiledArtifactIdentification> resolveDependency( CompiledDependencyIdentification dependencyIdentification);
}
