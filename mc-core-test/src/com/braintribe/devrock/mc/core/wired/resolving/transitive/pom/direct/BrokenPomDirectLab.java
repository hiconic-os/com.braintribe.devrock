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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.resolver.DeclaredArtifactCompiler;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifact;

public class BrokenPomDirectLab extends AbstractDirectPomCompilingTest {
	
	{
		launcher = Launcher.build()
				.repolet()
				.name("archive")
					.descriptiveContent()
						.descriptiveContent(RepoletContent.T.create())
					.close()
				.close()
				.done();
	}
	
	@Test
	public void testWithDottedVariables() {
		DeclaredArtifactCompiler compiler = resolverContext.declaredArtifactCompiler();		
		Maybe<CompiledArtifact> compiledArtifactM = compiler.compileReasoned( new File( codebaseRoot, "dotted-variable/pom.xml"));
		if (compiledArtifactM.isUnsatisfied()) {
			Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifactM.whyUnsatisfied().stringify() + "]");
			return;
		}
		else {
			CompiledArtifact compiledArtifact = compiledArtifactM.get();
			if (compiledArtifact.getInvalid())
				Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifact.getWhyInvalid().stringify() + "]");			
		}			
	}
	
	@Test
	public void testOnJacksonCore() {
		DeclaredArtifactCompiler compiler = resolverContext.declaredArtifactCompiler();
		Maybe<CompiledArtifact> compiledArtifactM = compiler.compileReasoned( new File( codebaseRoot, "jackson-core-2.9.9/pom.xml"));
		if (compiledArtifactM.isUnsatisfied()) {
			Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifactM.whyUnsatisfied().stringify() + "]");
			return;
		}
		else {
			CompiledArtifact compiledArtifact = compiledArtifactM.get();
			if (compiledArtifact.getInvalid())
				Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifact.getWhyInvalid().stringify() + "]");			
		}				
	}
	
	@Test
	public void testWithMissingEnvVariables() {
		DeclaredArtifactCompiler compiler = resolverContext.declaredArtifactCompiler();		
		Maybe<CompiledArtifact> compiledArtifactM = compiler.compileReasoned( new File( codebaseRoot, "missing-env-var/pom.xml"));
		if (compiledArtifactM.isUnsatisfied()) {
			Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifactM.whyUnsatisfied().stringify() + "]");
			return;
		}
		else {
			CompiledArtifact compiledArtifact = compiledArtifactM.get();
			if (compiledArtifact.getInvalid())
				Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifact.getWhyInvalid().stringify() + "]");			
		}
	}
	
	@Test
	public void testHttpMime() {
		DeclaredArtifactCompiler compiler = resolverContext.declaredArtifactCompiler();		
		Maybe<CompiledArtifact> compiledArtifactM = compiler.compileReasoned( new File( codebaseRoot, "httpmime/pom.xml"));
		if (compiledArtifactM.isUnsatisfied()) {
			Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifactM.whyUnsatisfied().stringify() + "]");
			return;
		}
		else {
			CompiledArtifact compiledArtifact = compiledArtifactM.get();
			if (compiledArtifact.getInvalid())
				Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifact.getWhyInvalid().stringify() + "]");			
		}
	}
	
	//@Test
	public void testAssertJCore() {
		DeclaredArtifactCompiler compiler = resolverContext.declaredArtifactCompiler();		
		Maybe<CompiledArtifact> compiledArtifactM = compiler.compileReasoned( new File( codebaseRoot, "assertj-core/pom.xml"));
		if (compiledArtifactM.isUnsatisfied()) {
			Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifactM.whyUnsatisfied().stringify() + "]");
			return;
		}
		else {
			CompiledArtifact compiledArtifact = compiledArtifactM.get();
			if (compiledArtifact.getInvalid())
				Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifact.getWhyInvalid().stringify() + "]");			
		}
		
	}
	
	@Test
	public void testMissingVersionTag() {
		DeclaredArtifactCompiler compiler = resolverContext.declaredArtifactCompiler();		
		Maybe<CompiledArtifact> compiledArtifactM = compiler.compileReasoned( new File( codebaseRoot, "missing-version-tag/pom.xml"));
		if (compiledArtifactM.isUnsatisfied()) {
			Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifactM.whyUnsatisfied().stringify() + "]");
			return;
		}
		else {
			CompiledArtifact compiledArtifact = compiledArtifactM.get();
			if (compiledArtifact.getInvalid())
				Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifact.getWhyInvalid().stringify() + "]");			
		}
	}

	@Test
	public void testMissingVersionVar() {
		DeclaredArtifactCompiler compiler = resolverContext.declaredArtifactCompiler();
		Maybe<CompiledArtifact> compiledArtifactM = compiler.compileReasoned( new File( codebaseRoot, "missing-version-var/pom.xml"));
		if (compiledArtifactM.isUnsatisfied()) {
			Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifactM.whyUnsatisfied().stringify() + "]");
			return;
		}
		else {
			CompiledArtifact compiledArtifact = compiledArtifactM.get();
			if (compiledArtifact.getInvalid())
				Assert.fail("Compiled artifact is unexpectedly invalid [" + compiledArtifact.getWhyInvalid().stringify() + "]");			
		}
	}

}
