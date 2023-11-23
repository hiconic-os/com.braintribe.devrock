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
package com.braintribe.devrock.mc.api.download;

import java.util.List;
import java.util.function.Function;

import com.braintribe.devrock.mc.impl.download.BasicPartEnrichingContext;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.consumable.PartEnrichment;

/**
 * @author pit/dirk
 *
 */
public interface PartEnrichingContext {
	/**
	 * @return - the enriching expert functional interface
	 */
	Function<AnalysisArtifact, List<PartEnrichment>> enrichmentExpert();
	
	
	/**
	 * @return - the {@link PartEnrichingContextBuilder} to build the {@link PartEnrichingContext}
	 */
	static PartEnrichingContextBuilder build() {
		return new BasicPartEnrichingContext();
	}
}
