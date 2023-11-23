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

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.version.Version;

public class SimpleTreeTest extends AbstractTransitiveResolverTest {

	@Override
	protected File archiveInput() {
		return new File( input, "simpleTree.definition.txt");
	}
	
	
	@Test
	public void testWithDependencyTerminal() {
		
		AnalysisArtifactResolution resolution = run( "com.braintribe.devrock.test:a#1.0.1", standardResolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		Validator validator = new Validator();
		validator.validate( new File ( input, "simpleTree.validation.yaml"), resolution);
		validator.assertResults();
	}
	
	@Test
	public void testWithArtifactTerminal() {
		CompiledArtifact artifact = CompiledArtifact.T.create();
		artifact.setGroupId("com.braintribe.devrock.test");
		artifact.setArtifactId("virtual-artifact");
		artifact.setVersion(Version.parse("1.0"));
		artifact.getDependencies().add(CompiledDependency.from(CompiledDependencyIdentification.parse("com.braintribe.devrock.test:a#1.0.1")));
		
		AnalysisArtifactResolution resolution = run( CompiledTerminal.from(artifact), standardResolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		Validator validator = new Validator();
		validator.validate( new File ( input, "simpleTree.validation.yaml"), resolution);
		validator.assertResults();
	}

}
