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

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class BrokenPomResolvingLab extends AbstractDirectPomCompilingTest {
	
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
	public void testPropertyResolutionProblem() {
		Maybe<CompiledArtifact> artifactMaybe = resolverContext.directCompiledArtifactResolver().resolve(CompiledArtifactIdentification.create("com.braintribe.devrock.test", "p", "1.0"));

		CompiledArtifact artifact = artifactMaybe.get();
		
		for (CompiledDependency dep: artifact.getDependencies()) {
			if (dep.getInvalid()) {
				System.out.println("\n : " + dep.asString());
				System.out.println(dep.getWhyInvalid().stringify());
			}
		}
	}	
	

	@Test
	public void testMissingVersionTag() {
		Maybe<CompiledArtifact> artifactMaybe = resolverContext.directCompiledArtifactResolver().resolve(CompiledArtifactIdentification.create("com.braintribe.devrock.test", "missing-version-tag", "1.0"));

		CompiledArtifact artifact = artifactMaybe.get();
		
		for (CompiledDependency dep: artifact.getDependencies()) {
			if (dep.getInvalid()) {
				System.out.println("\n - " + dep.asString());
				System.out.println("\t" + dep.getWhyInvalid().stringify());
			}
		}
	}
	@Test
	public void testMissingVersionVar() {
		Maybe<CompiledArtifact> artifactMaybe = resolverContext.directCompiledArtifactResolver().resolve(CompiledArtifactIdentification.create("com.braintribe.devrock.test", "missing-version-var", "1.0"));

		CompiledArtifact artifact = artifactMaybe.get();
		
		for (CompiledDependency dep: artifact.getDependencies()) {
			if (dep.getInvalid()) {
				System.out.println("\n - " + dep.asString());
				System.out.println("\t" + dep.getWhyInvalid().stringify());
			}
		}
	}	


	
	
}
