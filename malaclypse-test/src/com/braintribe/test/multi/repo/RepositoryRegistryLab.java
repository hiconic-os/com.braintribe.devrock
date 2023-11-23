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
