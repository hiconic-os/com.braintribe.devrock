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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.pom.direct;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;

public class BrokenPomTransitiveResolvingLab extends AbstractDirectPomCompilingTest {
	
	{
		launcher = Launcher.build()
				.repolet()
				.name("archive")
					.descriptiveContent()
						.descriptiveContent(archiveInput("archive"))
					.close()
				.close()
				.done();
	}
	
	
	
		
	@Test
	public void testMissingVersionVar() {
		CompiledArtifactIdentification compiledArtifactIdentification = CompiledArtifactIdentification.create("com.braintribe.devrock.test", "missing-version-var", "1.0");
		CompiledTerminal ct = CompiledTerminal.from(compiledArtifactIdentification);
		AnalysisArtifactResolution resolution = transitiveResolverContext.contract().transitiveDependencyResolver().resolve( standardTransitiveResolutionContext, ct);

		Assert.assertTrue("resolution unexpectedly did not fail", resolution.hasFailed());						
	}	

}
