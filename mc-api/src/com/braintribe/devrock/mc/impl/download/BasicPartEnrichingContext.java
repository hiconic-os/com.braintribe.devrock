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
