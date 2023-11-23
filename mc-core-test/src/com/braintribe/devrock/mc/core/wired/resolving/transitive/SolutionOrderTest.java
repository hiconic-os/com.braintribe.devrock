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
package com.braintribe.devrock.mc.core.wired.resolving.transitive;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class SolutionOrderTest extends AbstractTransitiveResolverTest {

	@Override
	protected File archiveInput() {
		return new File( input, "sortorder.definition.yaml");
	}

	private AnalysisArtifact generate( String name, int visitOrder, int dependencyOrder) {
		AnalysisArtifact artifact = AnalysisArtifact.T.create();
		artifact.setGroupId("com.braintribe.devrock.test");
		artifact.setArtifactId(name);
		artifact.setVersion( "1.0.1");
		artifact.setVisitOrder(visitOrder);
		artifact.setDependencyOrder(dependencyOrder);
		return artifact;
	}
	
	 @Test
	 public void runSolutionOrderingTest() {
		 
		 List<AnalysisArtifact> expectedSolutions = new ArrayList<>();
		 expectedSolutions.add( generate("t", 0, 6));
		 expectedSolutions.add( generate("a", 1, 5));
		 expectedSolutions.add( generate("b", 2, 4));
		 expectedSolutions.add( generate("c", 3, 1));
		 
		 expectedSolutions.add( generate("d", 5, 3));
		 expectedSolutions.add( generate("e", 4, 0));
		 
		 expectedSolutions.add( generate("f", 6, 2));
		 
		 AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", TransitiveResolutionContext.build().done());
		 
		 Validator validator = new Validator();
		 
		 validator.validate( new File( input,  "sortorder.definition.yaml"), resolution);
		 				 
		 validator.validateSolutionOrdering(resolution.getSolutions(), expectedSolutions);
		 validator.assertResults(); 
	 }
	
}
