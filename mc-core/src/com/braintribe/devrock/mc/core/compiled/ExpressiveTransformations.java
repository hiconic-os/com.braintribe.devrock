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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

public interface ExpressiveTransformations {
	/**
	 * transpose declared global redirects
	 * @param declared - the global redirects as a Map of Strings 
	 * @return - the global redirects as a Map of {@link CompiledDependencyIdentification}
	 */
	static Map<CompiledDependencyIdentification,CompiledDependencyIdentification> transformArtifactRedirects( Map<String, String> declared) {
		Map<CompiledDependencyIdentification,CompiledDependencyIdentification> transformed = new HashMap<>( declared.size());
		for (Map.Entry<String, String> entry : declared.entrySet()) {
			transformed.put( CompiledDependencyIdentification.parse( entry.getKey()), CompiledDependencyIdentification.parse(entry.getValue()));
		}
		return transformed;
	}
	
	/**
	 * transpose global dominant dependencies -> influences clash resolving 
	 * @param declared - the  {@link List} of dominant dependencies as String  
	 * @return - a {@link List} of {@link CompiledDependencyIdentification}
	 */
	static List<CompiledDependencyIdentification> transformDominants( List<String> declared) {
		List<CompiledDependencyIdentification> transformed = new ArrayList<>();
		for (String str : declared) {
			transformed.add( CompiledDependencyIdentification.parse( str));
		}
		return transformed;
	}
	
	/**
	 * transpose the global exclusions 
	 * @param declared - the global exclusions as a {@link Set} of {@link String}
	 * @return - a {@link Set} of {@link ArtifactIdentification}
	 */
	static Set<ArtifactIdentification> transformExclusions( Set<String> declared) {
		Set<ArtifactIdentification> transformed = new LinkedHashSet<>();
		for (String str : declared) {
			transformed.add( ArtifactIdentification.parse( str));
		}
		return transformed;
	}
	
}
