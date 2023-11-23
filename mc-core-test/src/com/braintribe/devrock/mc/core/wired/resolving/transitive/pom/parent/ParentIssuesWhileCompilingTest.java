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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.pom.parent;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class ParentIssuesWhileCompilingTest extends AbstractTransitiveResolverPomAndParentCompilingTest {

	
	@Test
	public void testPositive() {
			AnalysisArtifactResolution resolution = runAnalysis("com.braintribe.devrock.test:positive-parent-child#1.0.1");
			Assert.assertTrue("unexpectedly, the resolution failed", resolution.getFailure() == null);
	}
		
	public void testSetup() {
		try {
			RepositoryConfiguration configuration = getReflection().getRepositoryConfiguration();
			System.out.println();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testParentMissingDueToRange() {
			AnalysisArtifactResolution resolution = runAnalysis("com.braintribe.devrock.test:wrong-parent-range-child#1.0.1");
			Assert.assertTrue("unexpectedly, the resolution didn't fail", resolution.getFailure() != null);
	}
	
	@Test
	public void testParentMissingDueToNaming() {
			AnalysisArtifactResolution resolution = runAnalysis("com.braintribe.devrock.test:non-existing-parent-child#1.0.1");
			Assert.assertTrue("unexpectedly, the resolution didn't fail", resolution.getFailure() != null);
	}
	
	@Test
	public void testParentMissingDueMissingProperty() {
			AnalysisArtifactResolution resolution = runAnalysis("com.braintribe.devrock.test:missing-property-parent-child#1.0.1");
			Assert.assertTrue("unexpectedly, the compiler did pick-up an issue", resolution.getFailure() == null);
	}
}
