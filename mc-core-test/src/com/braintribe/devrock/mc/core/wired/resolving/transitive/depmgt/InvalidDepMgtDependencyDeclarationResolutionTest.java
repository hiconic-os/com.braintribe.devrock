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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.depmgt;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.braintribe.devrock.mc.core.compiler.configuration.origination.ReasoningHelper;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.mc.reason.MalformedManagedDependency;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests a simple setup with an improperly declared managed dependency
 * 
 * @author pit
 *
 */
public class InvalidDepMgtDependencyDeclarationResolutionTest extends AbstractDependencyManagementResolvingTest {

	@Override
	protected RepoletContent archiveInput() {	
		return loadInput( new File( input, "invalid.declared.depmgt.dependency.definition.yaml"));
	}
	

	@Test
	public void runSimpleParentStructure() {
		String terminal = "com.braintribe.devrock.test:t#1.0.1";
		AnalysisArtifactResolution artifactResolution = run(terminal, standardResolutionContext, false, null);
		
		Reason failure = artifactResolution.getFailure();
		
		Validator validator = new Validator();
		
		List<Reason> reasons = ReasoningHelper.extractAllReasons(failure, r -> r instanceof MalformedManagedDependency);
		
		validator.assertTrue("expected [1] MalformedManagedDependency reason in failure, yet found [" + reasons.size() + "]", reasons.size() == 1);
												
		validator.assertResults();
				
	}

	
}
