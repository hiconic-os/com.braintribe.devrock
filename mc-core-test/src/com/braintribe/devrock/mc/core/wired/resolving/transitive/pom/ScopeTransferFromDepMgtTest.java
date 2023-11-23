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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.pom;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests a situation where scopes from a dependency-management declaration overrides the scope of the actual referencing dependency. 
 * @author pit
 *
 */
public class ScopeTransferFromDepMgtTest extends AbstractTransitiveResolverPomCompilingTest {

	@Override
	protected RepoletContent archiveInput(String key) {		
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( new File( input, "scope.transfer.definition.yaml"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");			
		}
		return null;
	}
	
	
	@Override
	protected void runAdditionalBeforeSteps() {
		// no op		
	}


	@Test
	public void runScopeTransferTest() {
		AnalysisArtifactResolution resolution = runAnalysis("com.braintribe.devrock.test:t#1.0.1");
		
		Validator validator = new Validator();
		
		validator.validate( new File( input, "scope.transfer.validation.yaml"), resolution);		
		validator.assertResults();
	}

}
