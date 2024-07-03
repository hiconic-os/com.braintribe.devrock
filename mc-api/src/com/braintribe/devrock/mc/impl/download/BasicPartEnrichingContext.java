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
package com.braintribe.devrock.mc.impl.download;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.download.PartEnrichingContextBuilder;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.consumable.PartEnrichment;
import com.braintribe.model.artifact.essential.PartIdentification;

/**
 * the basic implementation of the {@link PartEnrichingContext}, in conjunction of being the {@link PartEnrichingContextBuilder} and finally,
 * the 'enriching expert' itself
 * 
 * @author pit / dirk
 *
 */
public class BasicPartEnrichingContext implements PartEnrichingContext, PartEnrichingContextBuilder, Function<AnalysisArtifact, List<PartEnrichment>> {

	private List<Function<AnalysisArtifact, List<PartEnrichment>>> enrichingExperts = new ArrayList<>();
	
	@Override
	public PartEnrichingContextBuilder enrichingExpert(
			Function<AnalysisArtifact, List<PartEnrichment>> enrichingExpert) {
		
		enrichingExperts.add(enrichingExpert);
		return this;
	}

	@Override
	public PartEnrichingContextBuilder enrichPart(PartIdentification partIdentification) {
		return enrichPart(partIdentification, false);
	}
	
	@Override
	public PartEnrichingContextBuilder enrichPart(PartIdentification partIdentification, boolean mandatory) {
		return enrichPart(partIdentification, mandatory, PartIdentification.asString(partIdentification));
	}
	
	@Override
	public PartEnrichingContextBuilder enrichPart(PartIdentification partIdentification, boolean mandatory,String key) {
		PartEnrichment partEnrichment = PartEnrichment.T.create();
		partEnrichment.setClassifier(partIdentification.getClassifier());
		partEnrichment.setType(partIdentification.getType());
		partEnrichment.setKey(key);
		partEnrichment.setMandatory(mandatory);
		
		return enrichingExpert(a -> Collections.singletonList(partEnrichment));
	}

	@Override
	public PartEnrichingContext done() {
		return this;
	}

	@Override
	public Function<AnalysisArtifact, List<PartEnrichment>> enrichmentExpert() {
		return this;
	}
	
	@Override
	public List<PartEnrichment> apply(AnalysisArtifact t) {
		switch (enrichingExperts.size()) {
		case 0:
			return Collections.emptyList();
		case 1:
			return enrichingExperts.get(0).apply(t);
		default:
			return enrichingExperts.stream().map(e -> e.apply(t)).flatMap(List::stream).collect(Collectors.toList());
		}

	}

}
