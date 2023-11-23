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
package com.braintribe.devrock.mc.api.js;

import java.util.Collections;

import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledTerminal;

public interface JsDependencyResolver {
	/**
	 * @param context - the {@link JsResolutionContext} with the configuration
	 * @param terminals - an {@link Iterable} of starting points as {@link CompiledTerminal}
	 * @return - an {@link AnalysisArtifactResolution} with the result
	 */
	AnalysisArtifactResolution resolve(JsResolutionContext context, Iterable<? extends CompiledTerminal> terminals);
	/**
	 * convenience method for {@link #resolve(JsResolutionContext, Iterable)}
	 * @param context - the {@link JsResolutionContext} with the configuration
	 * @param terminal - the single entry point as an {@link CompiledTerminal}
	 * @return - an {@link AnalysisArtifactResolution} with the result
	 */
	default AnalysisArtifactResolution resolve(JsResolutionContext context, CompiledTerminal terminal) {
		return resolve(context, Collections.singletonList(terminal));
	}
}
