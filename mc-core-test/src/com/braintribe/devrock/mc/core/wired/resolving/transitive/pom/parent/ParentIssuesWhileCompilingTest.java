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
