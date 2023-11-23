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

import org.junit.After;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.codebase.CodebaseRepositoryModule;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract base class for the various tests on 'direct pom compiling', i.e. reading poms not via resolving, 
 * but actually passing a file or a constructed (rather unmarshalled) DeclaredArtifact.
 * 
 * @author pit
 *
 */
public abstract class AbstractDirectPomCompilingTest implements HasCommonFilesystemNode {
	protected File repo;
	protected File codebaseRoot;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/pom/direct");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");
		codebaseRoot = new File( input, "codebase");
	}
		
	protected File initial = new File( input, "initial");

	protected RepoletContent archiveInput(String repoId) {
		File file = new File( input, repoId + ".content.definition.txt");
		try {
			return RepositoryGenerations.parseConfigurationFile(file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot load parser file [" + file.getAbsolutePath() + "]" , IllegalStateException::new);
		} 
	}
	protected WireContext<TransitiveResolverContract> transitiveResolverContext;
	protected ArtifactDataResolverContract resolverContext;
	
	
	protected RepositoryConfiguration repositoryConfiguration;

	protected Launcher launcher; 
	
	protected TransitiveResolutionContext standardTransitiveResolutionContext = TransitiveResolutionContext.build().lenient( true).done();
	protected ClasspathResolutionContext standardClasspathResolutionContext = ClasspathResolutionContext.build().lenient(false).done();
	
	/**
	 * @return - {@link RepositoryConfiguration} as required 
	 */
	private RepositoryConfiguration buildConfig() {

		RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.T.create();
		repositoryConfiguration.setLocalRepositoryPath(repo.getAbsolutePath());
		
		MavenHttpRepository repository = MavenHttpRepository.T.create();
		repository.setUrl("http://localhost:" + launcher.getAssignedPort() + "/archive");
		repository.setName("archive");
		
		repositoryConfiguration.getRepositories().add(repository);

		return repositoryConfiguration;
	}

	
	@Before
	public void runBefore() {
		TestUtils.ensure(repo); 			
		if (launcher != null) { 
			launcher.launch();
		}
		// copy initial data (mimic local repository)
		if (initial.exists()) {
			TestUtils.copy( initial, repo);
		}
				
		transitiveResolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, new CodebaseRepositoryModule(codebaseRoot, "${artifactId}"))
				.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(buildConfig()))				
				.build();
		
		resolverContext = transitiveResolverContext.contract().dataResolverContract();
	}
	
	@After
	public void runAfter() {
		if (launcher != null) {
			launcher.shutdown();
		}
		transitiveResolverContext.shutdown();
	}
	
	
	
}
