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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.pom.parent;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.wirings.codebase.CodebaseRepositoryModule;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract base class for all 'install repository purge' tests 
 * @author pit
 *
 * a) test the correct removal of a single artifact which is the only artifact (no other versions)
 * b) test the correct removal of a single artifact which does have versioned siblings
 * c) test the correct removal of multiple artifacts in one go
 */

public abstract class AbstractTransitiveResolverPomAndParentCompilingTest implements HasCommonFilesystemNode {

	protected static final String COMMON_CONTEXT_DEFINITION_YAML = "archive.definition.yaml";

	protected File repoCache;
	protected File repoInstall;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/pom/parent");
		input = pair.first;
		output = pair.second;
		repoCache = new File( output, "repo");
		repoInstall = new File( output, "install");
	}
	
	protected File config() { return new File( input, "repository-configuration.yaml");} 
	protected File initialCache = new File( input, "local-repo");
	protected File initialRepo = new File( input, "install-repo");
	
	protected TransitiveResolutionContext standardTransitiveResolutionContext = TransitiveResolutionContext.build().lenient( true).done();
	protected ClasspathResolutionContext standardClasspathResolutionContext = ClasspathResolutionContext.build().lenient(false).done();
	
	protected ArtifactDataResolverContract dataResolverContract;
	
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
	
	protected void additionalTasks() {}
	
	@Before
	public void runBefore() {
		
		
		// local repo -> cache
		TestUtils.ensure(repoCache);
		if (initialCache.exists()) {
			TestUtils.copy(initialCache, repoCache);
		}
		
		// install repo -> install 
		TestUtils.ensure(repoInstall);
		if (initialRepo.exists()) {
			TestUtils.copy(initialRepo, repoInstall);
		}	
		
		
		launcher.launch();		
		additionalTasks();		
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	
	protected RepoletContent archiveInput() {
		return archiveInput(COMMON_CONTEXT_DEFINITION_YAML);
	};		
	
	protected RepoletContent archiveInput(String definition) {
		File file = new File( input, definition);
		try {
			return RepositoryGenerations.unmarshallConfigurationFile(file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot load parser file [" + file.getAbsolutePath() + "]" , IllegalStateException::new);
		} 
	}
		
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("cache", repoCache.getAbsolutePath());
		ove.setEnv("install", repoInstall.getAbsolutePath());
		ove.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, config().getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}

	protected AnalysisArtifactResolution runAnalysis(String terminal) {
		VirtualEnvironment ove = buildVirtualEnvironement(null);
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, new EnvironmentSensitiveConfigurationWireModule( ove)).build();
			) {
							
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(terminal);
			CompiledTerminal ct = CompiledTerminal.from( cdi);
			return resolverContext.contract().transitiveDependencyResolver().resolve(standardTransitiveResolutionContext, ct);																				
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		return null;
	}
	
	protected RepositoryReflection getReflection() throws Exception {
		VirtualEnvironment ove = buildVirtualEnvironement(null);
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, new EnvironmentSensitiveConfigurationWireModule( ove)).build();
			) {			
			RepositoryReflection repositoryReflection = resolverContext.contract().dataResolverContract().repositoryReflection();			
			return repositoryReflection;																					
		}
	}
	
	
	
}
