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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.cycles;

import java.io.File;

import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * t -> a-:jar -> a-javadoc:jar
 * 
 * NOTE : CPR works just because javadoc's a 'well know part' and gets take because 'a' appears  
 * 
 * @author pit
 *
 */
public class DirectSelfReferenceWithDifferingNonContributingClassifiersTest extends AbstractTransitiveCycleTest {

	@Override
	protected File archiveInput() {
		return new File( input, "nonContributing.classifier.selfreference.definition.yaml");
	}
	
	private File validationInput() {
		return new File( input, "nonContributing.classifier.selfreference.validation.yaml");
	}


	@Test
	public void runTDR() {
		Pair<AnalysisArtifactResolution,Long> pair = resolveAsArtifact(terminal, standardTransitiveResolutionContext);
		AnalysisArtifactResolution resolution = pair.first;
		
		if (resolution.hasFailed()) {
			System.out.println( resolution.getFailure().asFormattedText());
		}
		else {
			Validator validator = new Validator();
			validator.validate( validationInput(), resolution, false, false);
			validator.assertResults();		
		}
	}
	
	/**
	 * works currently because a-javadoc:jar is enriched as it's a 'well known part' and gets enriched because of the 
	 * first a reference.
	 */
	@Test
	public void runCPR() {
		Pair<AnalysisArtifactResolution,Long> pair = resolveAsArtifact(terminal, standardClasspathResolutionContext);
		AnalysisArtifactResolution resolution = pair.first;
		
		if (resolution.hasFailed()) {
			System.out.println( resolution.getFailure().asFormattedText());			
		}
		else {
			Validator validator = new Validator();
			validator.validate( validationInput(), resolution, true, false);
			validator.assertResults();			
		}
	}
	
	
}
