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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.emptyclassifier;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class EmptyClassifierTest {
	@Test
	public void testEmptyClassifier() {
		try (WireContext<ClasspathResolverContract> context = Wire.context(EmptyClassifierWireModule.INSTANCE)) {
			ClasspathDependencyResolver classpathResolver = context.contract().classpathResolver();
			
			ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build().enrichJar(true).lenient(true).done();
			
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.create("test", "a", "1.0");
			
			AnalysisArtifactResolution resolution = classpathResolver.resolve(resolutionContext, cdi);
			
			Reason failure = resolution.getFailure();
			
			if (failure != null) {
				Assertions.fail("unexpected failure: " + failure.stringify());
			}
		}
	}
}
