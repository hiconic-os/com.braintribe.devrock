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
