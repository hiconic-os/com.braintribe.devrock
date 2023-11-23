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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.classpath.noncontributing;

import java.io.File;

import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests how a dependency with a 'non-contributing content' (from the point of view of the classpath) is treated. 
 * Currently, the solution appears in the CPR's result - and may create issues in post processors that do not expect 
 * non-contributing classifiers like 'javadoc:jar' (see AD-2582).
 * 
 * However, differing from old mc, mc-ng *is* removing dependencies that are filtered-out. 
 * 
 * @author pit
 *
 */
public class JavadocReferenceInClasspathTest extends AbstractNonContributingClasspathTest {

	@Override
	protected File archiveInput() {
		return new File( input, "nonContributing.classifier.selfreference.definition.yaml");
	}
	private File validationInputForTdr() {
		return new File( input, "tdr.nonContributing.classifier.selfreference.validation.yaml");
	}
	private File validationInputForCpr() {
		return new File( input, "cpr.nonContributing.classifier.selfreference.validation.yaml");
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
			validator.validate( validationInputForTdr(), resolution, false, false);
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
			validator.validate( validationInputForCpr(), resolution, true, false);
			validator.assertResults();			
		}
	}
	

}
