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
