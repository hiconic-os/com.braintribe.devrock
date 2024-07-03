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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.snapshots;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class RepoletSnaphotHandlingTest extends AbstractTransitiveSnapshotTest {

	@Override
	protected File archiveInput() {	
		return new File(input, "snapshot.definition.yaml");
	}

	@Test
	public void testTdrWithRangedSnapshotReference_local() {
		String terminal = COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.8";
		Pair<AnalysisArtifactResolution,Long> resolveAsArtifact = resolveAsArtifact( terminal, standardTransitiveResolutionContext);
		AnalysisArtifactResolution resolution = resolveAsArtifact.first;
		if (resolution.hasFailed()) {
			Assert.fail( "failed : " + resolution.getFailure().asFormattedText());
		}
		else {
			dumpResolution(resolution);
		}
		
	}
	
	@Test
	public void testCprWithRangedSnapshotReference_local() {
		String terminal = COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.8";
		Pair<AnalysisArtifactResolution,Long> resolveAsArtifact = resolveAsArtifact( terminal, standardClasspathResolutionContext);
		AnalysisArtifactResolution resolution = resolveAsArtifact.first;
		if (resolution.hasFailed()) {
			Assert.fail( "failed : " + resolution.getFailure().asFormattedText());
		}
		else {
			dumpResolution(resolution);
		}
		
	}

	
}
