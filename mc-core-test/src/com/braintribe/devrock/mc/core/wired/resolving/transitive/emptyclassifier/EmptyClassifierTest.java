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
