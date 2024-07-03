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
package com.braintribe.test.multi.repo;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.ArtifactReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.SolutionReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.ravenhurst.data.RepositoryRole;
import com.braintribe.test.framework.TestUtil;

public class RepositoryRegistryLab extends AbstractRepositoryRegistryLab {
	private static File settings = new File( "res/repositoryRegistryLab/contents/settings.user.xml");
	private static File localRepository = new File ("res/repositoryRegistryLab/contents/repo");
	private static File contents = new File( "res/repositoryRegistryLab/contents");
	private static final File [] data = new File[] { new File( contents, "archiveA.zip"), 
													 new File( contents, "archiveB.zip"),
													 new File( contents, "archiveC.zip"),
													 new File( contents, "archiveD.zip"),
	};
	private static LauncherShell launcherShell;


	@BeforeClass
	public static void before() {
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		localRepository.mkdirs();
		
		int port = runBefore();
		
		// clean local repository
		TestUtil.delete(localRepository);
			
		// fire them up 
		launchRepolets(port);
	
	}

	private static void launchRepolets( int port) {
		String [] args = new String[1];
		args[0] = 	"archiveA," + data[0].getAbsolutePath() + 
					";archiveB," + data[1].getAbsolutePath() + 
					";archiveC," + data[2].getAbsolutePath() +
					";archiveD," + data[3].getAbsolutePath();				
		launcherShell = new LauncherShell( port);
		//launchedRepolets = launcherShell.launch( args, RepoType.singleZip);
		launcherShell.launch( args, RepoType.singleZip);
	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}

	/**
	 * 
	 */
	@Test
	public void directTest() {
		Solution terminal;
		try {
			terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.subtreeexclusiontest");
			terminal.setArtifactId( "A");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
		} catch (VersionProcessingException e) {
			Assert.fail("cannot create test terminal [" + e.getLocalizedMessage() + "]");
			return;
		}
		
		try {
			ArtifactReflectionExpert artifactRepositoryExpert = repositoryRegistry.acquireArtifactReflectionExpert( terminal);
			List<Version> versions = artifactRepositoryExpert.getVersions( RepositoryRole.release, null);
			System.out.println(versions);
			
		} catch (RepositoryPersistenceException e) {
			Assert.fail("cannot setup and retrieve expert for test terminal artifact [" + e.getLocalizedMessage() + "]");
			return;
		}
		
		try {
			SolutionReflectionExpert solutionRepositoryExpert = repositoryRegistry.acquireSolutionReflectionExpert( terminal);
			Part part = Part.T.create();
			ArtifactProcessor.transferIdentification(part, terminal);
			part.setType( PartTupleProcessor.createPomPartTuple());
			File file = solutionRepositoryExpert.getPart(part, "A-1.0.pom", RepositoryRole.release);
			if (!file.exists()) {
				Assert.fail( "cannot retrieve expected file from repository");
			}
			System.out.println( file.getAbsolutePath());
		} catch (RepositoryPersistenceException e) {
			Assert.fail( "cannot setup and retrieve epert for test terminal solution [" + e.getLocalizedMessage() + "]");			
		}
		
		try {
			try {
				terminal.setVersion( VersionProcessor.createFromString("1.1"));		
			} catch (VersionProcessingException e) {
				Assert.fail("cannot create test terminal solution [" + e.getLocalizedMessage() + "]");
				return;
			}
			SolutionReflectionExpert solutionRepositoryExpert = repositoryRegistry.acquireSolutionReflectionExpert( terminal);
			Part part = Part.T.create();
			ArtifactProcessor.transferIdentification(part, terminal);
			part.setType( PartTupleProcessor.createPomPartTuple());
			File file = solutionRepositoryExpert.getPart(part, "A-1.1.pom", RepositoryRole.release);
			if (!file.exists()) {
				Assert.fail( "cannot retrieve expected file from repository");
				return;
			}
			System.out.println(file.getAbsolutePath());
		} catch (RepositoryPersistenceException e) {
			Assert.fail( "cannot setup and retrieve epert for test terminal solution [" + e.getLocalizedMessage() + "]");			
		}
		
	}
	
	

}
