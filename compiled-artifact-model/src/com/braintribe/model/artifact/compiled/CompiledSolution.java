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
package com.braintribe.model.artifact.compiled;

import com.braintribe.gm.model.reason.HasFailure;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Is a container for a dependency and its solution or a failure if that solution could not be found 
 * 
 * @author pit / dirk 
 *
 */
public interface CompiledSolution extends HasFailure {
	
	EntityType<CompiledSolution> T = EntityTypes.T(CompiledSolution.class);
	
	String solution = "solution";
	String dependency = "dependency";

	CompiledDependencyIdentification getDependency();
	void setDependency(CompiledDependencyIdentification dependency);
	
	/**
	 * @return - the {@link CompiledArtifact} which is the 'solution' to this dependency
	 */
	CompiledArtifact getSolution();
	void setSolution(CompiledArtifact value);
}
