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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.partreflection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.resolver.PartAvailabilityReflection;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.codebase.CodebaseRepositoryModule;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repository.CodebaseRepository;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * tests the 'active part reflection' with all currently known repository types, such {@link MavenHttpRepository} (REST Support), {@link MavenHttpRepository}(standard), 
 * {@link MavenFileSystemRepository}, {@link CodebaseRepository}, and local repository
 * 
 * @author pit
 *
 */

public class MultiRepositoryPartReflectionTest implements HasCommonFilesystemNode {
	private static final String FILESYSTEM = "filesystem";
	private static final String MAVEN = "maven";
	private static final String ARTIFACTORY = "artifactory";
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/partreflection");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	protected File initial = new File( input, "local-repo");
	protected File codebaseRoot = new File( input, "codebase-repo");
	protected File filesystem = new File( input, "filesystem-repo");
	
	protected Launcher launcher; 
	{
		launcher = Launcher.build()
				.repolet()
					.name(ARTIFACTORY)
					.descriptiveContent()
						.descriptiveContent( archiveInput( "artifactory.definition.txt"))
					.close()
					.restApiUrl("http://localhost:${port}/api/storage/artifactory")
					.serverIdentification("Artifactory/faked.by.repolet")			
				.close()
				.repolet()
					.name(MAVEN)
					.descriptiveContent()
						.descriptiveContent( archiveInput( "maven.definition.txt"))
					.close()						
				.close()
			.done();	
	}
	
	protected List<PartReflection> expectations() {
		List<PartReflection> expectations = new ArrayList<>(20);
	
		CompiledPartIdentification pomCpi = CompiledPartIdentification.create("com.braintribe.devrock.test", "t", "1.0.1", "pom");
		CompiledPartIdentification jarCpi = CompiledPartIdentification.create("com.braintribe.devrock.test", "t", "1.0.1", "jar");
		CompiledPartIdentification sourcesCpi = CompiledPartIdentification.create("com.braintribe.devrock.test", "t", "1.0.1", "sources", "jar");
		CompiledPartIdentification javadocCpi = CompiledPartIdentification.create("com.braintribe.devrock.test", "t", "1.0.1", "javadoc", "jar");
		CompiledPartIdentification assetCpi = CompiledPartIdentification.create("com.braintribe.devrock.test", "t", "1.0.1", "asset", "man");
		
		// artifactory 
		expectations.add(PartReflection.from( pomCpi, ARTIFACTORY));
		expectations.add(PartReflection.from( jarCpi, ARTIFACTORY));
		expectations.add(PartReflection.from( sourcesCpi, ARTIFACTORY));
		expectations.add(PartReflection.from( javadocCpi, ARTIFACTORY));
		expectations.add(PartReflection.from( assetCpi, ARTIFACTORY));
		
		// maven 
		expectations.add(PartReflection.from( pomCpi, MAVEN));
		expectations.add(PartReflection.from( jarCpi, MAVEN));
		expectations.add(PartReflection.from( sourcesCpi, MAVEN));
		expectations.add(PartReflection.from( javadocCpi, MAVEN));
		expectations.add(PartReflection.from( assetCpi, MAVEN));
		
		// filesystem 
		expectations.add(PartReflection.from( pomCpi, FILESYSTEM));
		expectations.add(PartReflection.from( jarCpi, FILESYSTEM));
		expectations.add(PartReflection.from( sourcesCpi, FILESYSTEM));
		expectations.add(PartReflection.from( javadocCpi, FILESYSTEM));
		
		// local 
		expectations.add(PartReflection.from( pomCpi, "local"));
		expectations.add(PartReflection.from( jarCpi, "local"));
		expectations.add(PartReflection.from( sourcesCpi, "local"));
		expectations.add(PartReflection.from( javadocCpi, "local"));
		
		// codebase 
		expectations.add(PartReflection.from( pomCpi, "codebase"));
		
		return expectations;
		
	}
	
		
	/**
	 * creates a repo conf with 3 repos, codebase is injected below and local is there anyhow
	 * @return - the {@link RepositoryConfiguration} to use
	 */
	protected RepositoryConfiguration repositoryConfiguration()	{
		RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.T.create();
		repositoryConfiguration.setLocalRepositoryPath( repo.getAbsolutePath());
		
		// artifactory repolet
		MavenHttpRepository artifactoryRepo = MavenHttpRepository.T.create();
		artifactoryRepo.setName(ARTIFACTORY);
		artifactoryRepo.setUrl( launcher.getLaunchedRepolets().get(ARTIFACTORY));
		repositoryConfiguration.getRepositories().add(artifactoryRepo);
		
		// maven repolet
		MavenHttpRepository mavenRepo = MavenHttpRepository.T.create();
		mavenRepo.setName(MAVEN);
		mavenRepo.setUrl( launcher.getLaunchedRepolets().get(MAVEN));
		repositoryConfiguration.getRepositories().add(mavenRepo);
		
		// maven filesystem		
		MavenFileSystemRepository filesystemRepo = MavenFileSystemRepository.T.create();
		filesystemRepo.setName( FILESYSTEM);		
		filesystemRepo.setRootPath( filesystem.getAbsolutePath());		
		repositoryConfiguration.getRepositories().add( filesystemRepo);
				
		return repositoryConfiguration;
	}
	
	@Before
	public void runBefore() {		
		TestUtils.ensure(repo); 
		TestUtils.copy( initial, repo);
		launcher.launch();			
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	

	protected RepoletContent archiveInput(String definition) {
		File file = new File( input, definition);
		try {
			return RepositoryGenerations.parseConfigurationFile(file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot load parser file [" + file.getAbsolutePath() + "]" , IllegalStateException::new);
		} 
	}
	
	
	/**
	 * run a resolution first and then extract the locally known part availability 
	 * @param terminalAsString - the terminal for the resolution 
	 * @param availabilityTargetAsString - the availability target
	 * @return - a {@link Map} of the repoid to a {@link Set} of the found {@link CompiledPartIdentification}
	 */
	protected List<PartReflection> runResolve(String availabilityTargetAsString) {
		
		
		CodebaseRepositoryModule codebaseModule = new CodebaseRepositoryModule( codebaseRoot, "${artifactId}");
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, codebaseModule)
					.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(repositoryConfiguration()))				
					.build();
			) {
			
			RepositoryConfiguration repositoryConfiguration = resolverContext.contract().dataResolverContract().repositoryReflection().getRepositoryConfiguration();
			
			// retrieve the part availability data
			CompiledArtifactIdentification compiledTargetIdentification = CompiledArtifactIdentification.parse(availabilityTargetAsString);
			
			
			PartAvailabilityReflection partAvailabilityReflection = resolverContext.contract().dataResolverContract().partAvailabilityReflection();
			List<PartReflection> partsOf = partAvailabilityReflection.getAvailablePartsOf(compiledTargetIdentification);
						
			return partsOf;
											
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		return null;
	}
	
	@Test
	public void testActivePartReflectionOverAllKindsOfRepositories() {
		List<PartReflection> partsOf = runResolve( "com.braintribe.devrock.test:t#1.0.1");
		System.out.println("found [" + partsOf.size() + "] parts");
		validate(partsOf, expectations());
	}
	
	/**
	 * validates two part-availability extractions (found real, expected constructed)
	 * @param found - a {@link Map} of the repoid to a {@link Set} of the found {@link CompiledPartIdentification}
	 * @param expected - a {@link Map} of the repoid to a {@link Set} of the found {@link CompiledPartIdentification}
	 * @return - true if matches, otherwise false (will actually throw an assertion failure)
	 */
	protected boolean validate( List<PartReflection> found, List<PartReflection> expected) {
		List<String> assertions = new ArrayList<>();
		
		if (found.size() != expected.size()) {
			assertions.add("size of found data expected to be [" + expected.size() + "] yet it is [" + found.size() + "]");
		}
		
	
		List<String> foundParts = found.stream().map( cp -> cp.asString()).collect(Collectors.toList());			
		List<String> expectedParts = expected.stream().map( cp -> cp.asString()).collect( Collectors.toList());
		
		List<String> matching = new ArrayList<>( foundParts.size());
		List<String> excess = new ArrayList<>( foundParts.size());			
		for (String foundCpi : foundParts) {
			if (expectedParts.contains( foundCpi)) {
				matching.add( foundCpi);
			}
			else {
				excess.add( foundCpi);
			}
		}
		List<String> missing = new ArrayList<>( expectedParts);
		missing.removeAll( matching);
		
		if (missing.size() > 0) {
			assertions.add( "[" + missing.size() + "] missing : " + missing.stream().collect(Collectors.joining(",")));
		}
		if (excess.size() > 0) {
			assertions.add( "[" + excess.size() + "] excess : " + excess.stream().collect(Collectors.joining(",")));
		}
	
		
		if (assertions.size() > 0) {
			Assert.fail( assertions.stream().collect( Collectors.joining("\n")));
			return false;		
		}
		return true;
	}
	
}
