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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.parents;

import java.io.File;

import org.junit.Test;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests a simple setup with one parent and one import inside this parent
 * 
 * @author pit
 *
 */
public class SingleParentResolutionTest extends AbstractParentResolutionResolvingTest {

	@Override
	protected RepoletContent archiveInput() {	
		return loadInput( new File( input, "parents.definition.yaml"));
	}
	

	@Test
	public void runSimpleParentStructure() {
		String terminal = "com.braintribe.devrock.test:t#1.0.1";
		AnalysisArtifactResolution artifactResolution = run(terminal, standardResolutionContext, false, null);
		
		Validator validator = new Validator();		
		validator.validate( new File( input, "parents.validation.yaml"), artifactResolution);
		boolean valid = validator.assertResults();
		
		if (!valid) {
			return;
		}
		
		// dump
		String name = buildDumpFilename(terminal, "enriched");
		dump(new File( output, name), artifactResolution);
		
		
	}

	
}
