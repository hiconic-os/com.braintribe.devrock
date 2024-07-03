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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.context.filter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.consumable.PartEnrichment;

/**
 * tests the 'enriching expert' on both TDR and CPR
 * 
 * @author pit
 *
 */
public class EnrichingFilterTest extends AbstractResolvingContextTest {

	@Override
	protected RepoletContent archiveInput() {		
		return archiveInput(COMMON_CONTEXT_DEFINITION_YAML);
	}
	
	
	private List<PartEnrichment> getPartEnrichments( AnalysisArtifact aa) {
		String aid = aa.getArtifactId();
		if (aid.equals("a") || aid.equals("b")) {
			PartEnrichment pe = PartEnrichment.T.create();
			pe.setType("data");
			pe.setMandatory(true);
			return Collections.singletonList( pe);
		}
		return Collections.emptyList();
	}
	
	
	@Test 
	public void runPartEnrichingContextOnTDR() {
		PartEnrichingContext pec = PartEnrichingContext.build().enrichingExpert( this::getPartEnrichments).done();		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().enrich(pec).done();
		AnalysisArtifactResolution resolution = runAndValidate(trc, COMMON_CONTEXT_DEFINITION_YAML);
		// 
		List<AnalysisArtifact> artifactsToCheckTheParts = resolution.getSolutions().stream().filter( a -> a.getArtifactId().equals("a") || a.getArtifactId().equals("b")).collect(Collectors.toList());
		for (AnalysisArtifact aa : artifactsToCheckTheParts) {
			Map<String, Part> parts = aa.getParts();
			Assert.assertTrue("artifact [" + aa.asString() + "] doesn't have the expected part", parts.containsKey( ":data"));			
		}		
	}
	

	@Test 
	public void runPartEnrichingContextOnCPR() {
		PartEnrichingContext pec = PartEnrichingContext.build().enrichingExpert( this::getPartEnrichments).done();		
		ClasspathResolutionContext trc = ClasspathResolutionContext.build().enrich(pec).done();
		AnalysisArtifactResolution resolution = runAndValidate(trc, COMMON_CONTEXT_DEFINITION_YAML);
		List<AnalysisArtifact> artifactsToCheckTheParts = resolution.getSolutions().stream().filter( a -> a.getArtifactId().equals("a") || a.getArtifactId().equals("b")).collect(Collectors.toList());
		for (AnalysisArtifact aa : artifactsToCheckTheParts) {
			Map<String, Part> parts = aa.getParts();
			Assert.assertTrue("artifact [" + aa.asString() + "] doesn't have the expected part", parts.containsKey( ":data"));			
		}
	}

}
