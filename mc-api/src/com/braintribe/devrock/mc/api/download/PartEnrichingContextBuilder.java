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
