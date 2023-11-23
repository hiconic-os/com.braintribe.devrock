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
package com.braintribe.processing.panther.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import com.braintribe.testing.category.KnownIssue;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.model.processing.panther.depmgt.FullWalk;
import com.braintribe.processing.panther.test.wire.contract.ExternalConfigurationContract;
import com.braintribe.processing.panther.test.wire.contract.TestMainContract;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

@Category(KnownIssue.class)
public class BuildWalkTest {
	
	
	

	@Test
	public void fullWalkTest() throws Exception {
		SourceRepository sourceRepository = SourceRepository.T.create();
		sourceRepository.setRepoUrl("https://svn.braintribe.com/repo/master/Development/artifacts/");
		
		Set<SourceArtifact> sourceArtifacts = new HashSet<>(Arrays.asList(
				createSourceArtifact(sourceRepository, "com.braintribe.model.processing", "GmCoreApi", "1.2"),
				createSourceArtifact(sourceRepository, "com.braintribe.model.processing", "GmCore", "1.2")
		));
		
		File walkCache = ensureWalkCache();
		
		ExternalConfigurationContractImpl externalConfiguration = new ExternalConfigurationContractImpl(sourceArtifacts, walkCache);
		
		WireContext<TestMainContract> wireContext = Wire
				.context(TestMainContract.class)
				.bindContracts("com.braintribe.processing.panther.test.wire")
				.bindContract(ExternalConfigurationContract.class, externalConfiguration)
				.build();

		TestMainContract testMainContext = wireContext.beans();
		
		FullWalk fullWalk = new FullWalk(testMainContext.pomReader(), testMainContext.dependencyResolver(), sourceArtifacts);
		
		List<Solution> solutions = fullWalk.walk(true, null);

		for (Solution solution: solutions) {
			System.out.println(NameParser.buildName(solution));
		}
	}
	
	private static SourceArtifact createSourceArtifact(SourceRepository repository, String groupId, String artifactId, String version) {
		SourceArtifact artifact = SourceArtifact.T.create();
		artifact.setGroupId(groupId);
		artifact.setArtifactId(artifactId);
		artifact.setVersion(version);
		artifact.setRepository(repository);
		artifact.setPath(groupId.replace('.', '/') + "/" + artifactId + "/" + version);
		
		return artifact;
	}
	
	
	//@Test
	public void simpleWalkTest() throws Exception {
		
		SourceRepository sourceRepository = SourceRepository.T.create();
		sourceRepository.setRepoUrl("https://svn.braintribe.com/repo/master/Development/artifacts/");
		
		SourceArtifact artifact = SourceArtifact.T.create();
		artifact.setGroupId("com.braintribe.test.dependencies.parentTest");
		artifact.setArtifactId("Parent");
		artifact.setVersion("1.0");
		artifact.setRepository(sourceRepository);
		artifact.setPath("com/braintribe/test/dependencies/parentTest/Parent/1.0");
		
		Set<SourceArtifact> sourceArtifacts = new HashSet<>(Arrays.asList(artifact));
		
		File walkCache = ensureWalkCache();
		
		ExternalConfigurationContract externalConfiguration = new ExternalConfigurationContractImpl(sourceArtifacts, walkCache);
		
		WireContext<TestMainContract> wireContext = Wire
				.context(TestMainContract.class)
				.bindContracts("com.braintribe.processing.panther.test.wire")
				.bindContract(ExternalConfigurationContract.class, externalConfiguration)
				.build();
		
		
		String walkScopeId = UUID.randomUUID().toString();
		
		ArtifactPomReader pomReader = wireContext.beans().pomReader();
		
		Solution solution = pomReader.read(walkScopeId, "com.braintribe.test.dependencies.parentTest", "ParentTestTerminal", "1.0");
		
		
		
		Solution parent = solution.getResolvedParent();
		
		if (parent != null) {
			System.out.println(NameParser.buildName(parent));
			
		}
		
		for (Dependency dependency: solution.getManagedDependencies()) {
			if (dependency.getScope().equalsIgnoreCase("import")) {
				System.out.println(NameParser.buildName(dependency));
			}
		}
		
		for (Dependency dependency: solution.getDependencies()) {
			System.out.println(NameParser.buildName(dependency));
		}
		
		
		/*
		while (parent != null) {
			for (Dependency dependency: parent.getDependencies()) {
				System.out.println(NameParser.buildName(dependency));
			}
			
			parent = parent.getResolvedParent();
		}
		*/
	}

	private File ensureWalkCache() throws IOException {
		File walkCache = new File("walkCache");
		
		if (walkCache.exists()) {
		
			Files.walk(walkCache.toPath(), FileVisitOption.FOLLOW_LINKS)
			    .sorted(Comparator.reverseOrder())
			    .forEach(p -> {
			    	try {
						Files.delete(p);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
			    });
		}
		
		walkCache.mkdirs();
		return walkCache;
	}
	
	private static class ExternalConfigurationContractImpl implements ExternalConfigurationContract {
		private Set<SourceArtifact> sourceArtifacts;
		private File file;
		
		public ExternalConfigurationContractImpl(Set<SourceArtifact> sourceArtifacts, File file) {
			super();
			this.sourceArtifacts = sourceArtifacts;
			this.file = file;
		}

		@Override
		public Set<SourceArtifact> sourceArtifacts() {
			return sourceArtifacts;
		}
		
		@Override
		public File walkCache() {
			return file;
		}
	}
}
