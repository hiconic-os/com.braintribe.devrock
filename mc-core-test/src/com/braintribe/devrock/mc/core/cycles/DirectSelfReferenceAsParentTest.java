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
package com.braintribe.devrock.mc.core.cycles;

import java.io.File;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;

import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.core.compiler.configuration.origination.ReasoningHelper;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.model.mc.reason.ParentCompilationCycle;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

public class DirectSelfReferenceAsParentTest extends AbstractCycleTest {

	
	@Override
	protected File archiveContentDirectory() {
		return new File( input, "directSelfReferenceAsParent");
	}

	@Override
	protected void runTest(ArtifactDataResolverContract contract) {
			CompiledArtifactResolver compiledArtifactResolver = contract.redirectAwareCompiledArtifactResolver();
			Maybe<CompiledArtifact> resolved = compiledArtifactResolver.resolve( CompiledArtifactIdentification.parse(TERMINAL));
			
		if (!resolved.get().getInvalid())
			Assert.fail("artifact unexpectedly not marked as failed");
		
		List<Reason> reasons = ReasoningHelper.extractAllReasons(resolved.get().getWhyInvalid(), r -> r instanceof ParentCompilationCycle);
		
		Assertions.assertThat(reasons.size()).as("ParentCompilationCycle not correctly detected").isEqualTo(1);
	}
	
}
