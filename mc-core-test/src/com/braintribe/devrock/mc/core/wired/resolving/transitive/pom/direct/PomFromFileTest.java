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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.devrock.mc.api.resolver.DeclaredArtifactCompiler;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.declared.DeclaredArtifact;

/**
 * test for direct (File or {@link DeclaredArtifact}) compiling of artifacts 
 * @author pit
 *
 */
public class PomFromFileTest extends AbstractDirectPomCompilingTest {
	private static DeclaredArtifactMarshaller marshaller = DeclaredArtifactMarshaller.INSTANCE;
	
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
	public void compileStandalonePomFileFromCodebase() {
		DeclaredArtifactCompiler compiler = resolverContext.declaredArtifactCompiler();
		
		Maybe<CompiledArtifact> compiledArtifactM = compiler.compileReasoned( new File( codebaseRoot, "standalone/pom.xml"));
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
	public void compileStandaloneDeclaredPomFromCodebase() {
		DeclaredArtifactCompiler compiler = resolverContext.declaredArtifactCompiler();
		DeclaredArtifact declaredArtifact = null;
		try (InputStream in = new FileInputStream( new File( codebaseRoot, "standalone/pom.xml"))) {
			declaredArtifact = marshaller.unmarshall(in);
		}
		catch (Exception e) {
			Assert.fail("cannot unmarshall artifact as " + e.getMessage());
		}		
		Maybe<CompiledArtifact> compiledArtifactM = compiler.compileReasoned( declaredArtifact);
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
	public void compilePomFileFromCodebaseWithRemoteParent() {
		DeclaredArtifactCompiler compiler = resolverContext.declaredArtifactCompiler();		
		Maybe<CompiledArtifact> compiledArtifactM = compiler.compileReasoned( new File( codebaseRoot, "remote-parent-ref/pom.xml"));
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
	public void compileDeclaredPomFromCodebaseWithRemoteParent() {
		DeclaredArtifactCompiler compiler = resolverContext.declaredArtifactCompiler();
		DeclaredArtifact declaredArtifact = null;
		try (InputStream in = new FileInputStream( new File( codebaseRoot, "remote-parent-ref/pom.xml"))) {
			declaredArtifact = marshaller.unmarshall(in);
		}
		catch (Exception e) {
			Assert.fail("cannot unmarshall artifact as " + e.getMessage());
		}
		Maybe<CompiledArtifact> compiledArtifactM = compiler.compileReasoned( declaredArtifact);
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
