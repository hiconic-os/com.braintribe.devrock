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
