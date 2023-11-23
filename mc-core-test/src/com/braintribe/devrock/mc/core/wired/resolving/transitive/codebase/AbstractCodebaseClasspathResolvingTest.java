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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.codebase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.codebase.CodebaseRepositoryModule;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
@Category(KnownIssue.class)
public abstract class AbstractCodebaseClasspathResolvingTest implements HasCommonFilesystemNode {

	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/codebase");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	protected TransitiveResolutionContext standardResolutionContext = TransitiveResolutionContext.build().done();
			
	protected abstract RepoletContent archiveInput();	
	
	
	protected RepoletContent archiveInput(String definition) {
		File file = new File( input, definition);
		try {
			return RepositoryGenerations.parseConfigurationFile( file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot parse file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 
	}
	
	
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
	public void runBefore() throws IOException {
		File logConfig = new File("res", "input/logging.properties");
		
		LogManager manager = LogManager.getLogManager();

		try (InputStream in = new FileInputStream(logConfig)) {
			manager.readConfiguration(in);
		}

		
		TestUtils.ensure(repo); 			
		launcher.launch();			
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	
	protected final AnalysisArtifactResolution run(String terminal, ClasspathResolutionContext resolutionContext, File codebaseRoot, String template) {		
		return run( terminal, resolutionContext, new CodebaseRepositoryModule(codebaseRoot, template));
	}
	
	@SafeVarargs
	protected final AnalysisArtifactResolution run(String terminal, ClasspathResolutionContext resolutionContext, Pair<File,String> ... codebasePairs) {
		return run( terminal, resolutionContext, new CodebaseRepositoryModule(codebasePairs));
	}
	
	protected AnalysisArtifactResolution run(String terminal, ClasspathResolutionContext resolutionContext, CodebaseRepositoryModule module) {
		MavenHttpRepository repository = MavenHttpRepository.T.create();
		repository.setUrl("http://localhost:" + launcher.getAssignedPort() + "/archive");
		repository.setName("archive");
		
		RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.T.create();
		repositoryConfiguration.setLocalRepositoryPath(repo.getAbsolutePath());
		repositoryConfiguration.getRepositories().add(repository);
		
		try (				
				WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, module)
					.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(repositoryConfiguration))				
					.build();
			) {
			
			ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
			
			CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));
			AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cdi);
			return artifactResolution;					
								
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		return null;
	}
	
}
