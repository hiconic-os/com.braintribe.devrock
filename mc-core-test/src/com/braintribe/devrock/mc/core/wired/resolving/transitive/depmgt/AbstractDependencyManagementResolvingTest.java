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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.depmgt;

import java.io.File;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * @author pit
 *
 */
@Category(KnownIssue.class)
public abstract class AbstractDependencyManagementResolvingTest implements HasCommonFilesystemNode {

	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/depmgt");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	private File settings = new File( input, "settings.xml");
	
	protected ClasspathResolutionContext standardResolutionContext = ClasspathResolutionContext.build().lenient(true).done();
			
	protected abstract RepoletContent archiveInput();	
		
	private Launcher launcher; 
	{
		launcher = Launcher.build()
				.repolet()
				.name("archive")
					.descriptiveContent()
						.descriptiveContent(archiveInput())
					.close()
				.close()
			.done();
	}

	@Before
	public void runBefore() {
		TestUtils.ensure(repo); 			
		launcher.launch();
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	
	
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("M2_REPO", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}
	protected AnalysisArtifactResolution run(String terminal, ClasspathResolutionContext resolutionContext, String suffix) {
		return run(terminal, resolutionContext, false, suffix);
	}
	
	protected AnalysisArtifactResolution run(String terminal, ClasspathResolutionContext resolutionContext, boolean expectException, String suffix) {
		boolean thrown = false;
		try (				
				WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
			
			ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
			
			CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));			
			AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cdi);
			
			if (suffix == null) {
				return artifactResolution;
			}			
			return artifactResolution;					
								
		}
		
		catch( Exception e) {
			e.printStackTrace();
			thrown = true;
			if (!expectException) {
				Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");
			}
		}
		if (expectException && !thrown) {
			Assert.fail("expected exception not thrown ");
		}
		return null;
	}
	
	
	protected AnalysisArtifactResolution runAsArtifact(String terminal, ClasspathResolutionContext resolutionContext, String suffix) {
		return runAsArtifact(terminal, resolutionContext, false, suffix);
	}
	protected AnalysisArtifactResolution runAsArtifact(String terminal, ClasspathResolutionContext resolutionContext, boolean expectException, String suffix) {
		boolean thrown = false;
		try (				
				WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
			
			ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
			CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(terminal);
			Maybe<CompiledArtifact> compiledArtifactOptional = resolverContext.contract().transitiveResolverContract().dataResolverContract().directCompiledArtifactResolver().resolve( cai);
						
			CompiledTerminal cdi = compiledArtifactOptional.get();			
			AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cdi);
			
			if (suffix == null) {
				return artifactResolution;
			}			
			return artifactResolution;					
								
		}
		catch( Exception e) {
			e.printStackTrace();
			thrown = true;
			if (!expectException) {
				Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");
			}
		}
		if (expectException && !thrown) {
			Assert.fail("expected exception not thrown ");
		}
		return null;
	}
	
	
	protected Stream<Part> getCpJarParts(AnalysisArtifact artifact) {
		return artifact.getParts().entrySet().stream().filter(e -> e.getKey().endsWith(":jar")).map(Map.Entry::getValue);
	}

	protected RepoletContent loadInput(File file) {		
		try {
			if (file.getName().endsWith(".yaml")) {
				return RepositoryGenerations.unmarshallConfigurationFile(file);
			}
			else {
				return RepositoryGenerations.parseConfigurationFile( file);
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot parse file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 
	}	
	
}
