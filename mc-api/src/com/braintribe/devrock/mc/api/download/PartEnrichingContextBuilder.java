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

import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.consumable.PartEnrichment;
import com.braintribe.model.artifact.essential.PartIdentification;

/**
 * a builder for the {@link PartEnrichingContext}
 * @author pit/dirk
 *
 */
public interface PartEnrichingContextBuilder {
	/**
	 * @param enrichingExpert
	 * @return
	 */
	PartEnrichingContextBuilder enrichingExpert(Function<AnalysisArtifact, List<PartEnrichment>> enrichingExpert);
	
	/**
	 * optionally enrich
	 * @param partIdentification - the {@link PartIdentification} to leniently enrich
	 * @return - itself, the {@link PartEnrichingContextBuilder}
	 */
	PartEnrichingContextBuilder enrichPart(PartIdentification partIdentification);
	
	/**
	 * @param partIdentification - the {@link PartIdentification} to enrich
	 * @param mandatory - true if non-existence should lead to an error, false if lenient
	 * @return - itself, the {@link PartEnrichingContextBuilder} 
	 */
	PartEnrichingContextBuilder enrichPart(PartIdentification partIdentification, boolean mandatory);
	/**
	 * @param partIdentification - the {@link PartIdentification} to enrich
	 * @param mandatory - true if non-existence should lead to an error, false if lenient
	 * @param key - the key of the part, as it should be marked amongst the artifact's parts
	 * @return - itself, the {@link PartEnrichingContextBuilder}
	 */
	PartEnrichingContextBuilder enrichPart(PartIdentification partIdentification, boolean mandatory, String key);
	
	/**
	 * @return - the {@link PartEnrichingContext} created
	 */
	PartEnrichingContext done();
}
