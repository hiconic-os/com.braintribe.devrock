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
package com.braintribe.devrock.mc.api.classpath;

import java.util.Collections;

import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledTerminal;

/**
 * the resolver that handles classpath logic, i.e. resolves clashes 
 * 
 * @author pit / dirk
 *
 */
public interface ClasspathDependencyResolver {
	/**
	 * @param context - the {@link ClasspathResolutionContext} with the configuration
	 * @param terminals - an {@link Iterable} of starting points as {@link CompiledTerminal}
	 * @return - an {@link AnalysisArtifactResolution} with the result
	 */
	AnalysisArtifactResolution resolve(ClasspathResolutionContext context, Iterable<? extends CompiledTerminal> terminals);
	
	/**
	 * convenience method for {@link #resolve(ClasspathResolutionContext, Iterable)}
	 * @param context - the {@link ClasspathResolutionContext} with the configuration
	 * @param terminal - the single entry point as an {@link CompiledTerminal}
	 * @return - an {@link AnalysisArtifactResolution} with the result
	 */
	default AnalysisArtifactResolution resolve(ClasspathResolutionContext context, CompiledTerminal terminal) {
		return resolve(context, Collections.singletonList(terminal));
	}
}
