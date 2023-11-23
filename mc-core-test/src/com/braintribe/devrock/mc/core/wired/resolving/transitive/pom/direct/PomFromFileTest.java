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
