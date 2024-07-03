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
package com.braintribe.devrock.mc.api.transitive;

import java.util.Collections;

import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledTerminal;

/**
 * the {@link TransitiveDependencyResolver}, i.e what handles build walks, repository extractions, and is the base for 
 * the {@link ClasspathDependencyResolver}
 * 
 * @author pit / dirk
 *
 */
public interface TransitiveDependencyResolver {
	/**
	 * @param context - the {@link TransitiveResolutionContext} containing the configuration 
	 * @param terminals - an {@link Iterable} with the starting points, as {@link CompiledTerminal}
	 * @return - the resulting {@link AnalysisArtifactResolution}
	 */
	AnalysisArtifactResolution resolve(TransitiveResolutionContext context, Iterable<? extends CompiledTerminal> terminals);
	
	/**
	 * @param context - the {@link TransitiveResolutionContext} containing the configuration
	 * @param terminal - the single starting point as {@link CompiledTerminal}
	 * @return - the resulting {@link AnalysisArtifactResolution}
	 */
	default AnalysisArtifactResolution resolve(TransitiveResolutionContext context, CompiledTerminal terminal) {
		return resolve(context, Collections.singletonList(terminal));
	}
}
