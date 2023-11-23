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
package com.braintribe.test.multi.repo.information;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RetrievalMode;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifacts.mc.wire.classwalk.ClasspathResolvers;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ConfigurableClasspathResolverExternalSpace;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.Scopes;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.info.ArtifactInformation;
import com.braintribe.model.artifact.info.LocalRepositoryInformation;
import com.braintribe.model.artifact.info.PartInformation;
import com.braintribe.model.artifact.info.RemoteRepositoryInformation;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract test class for the wired CP walker.. 
 *  
 * @author pit
 *
 */
public abstract class AbstractRepositoryInformationRetrievalTest {
	protected static RepositoryReflection repoReflection;
	protected static File contents = new File( "res/repositoryInformationRetrieval/contents");
	private static final File [] data = new File[] { new File( contents, "archiveBase.zip"), 
			 							new File( contents, "archiveComplement.zip"),};
	private static LauncherShell launcherShell;

	
	protected static void before(File settings, File localRepository) {
		int port = NetworkTools.getUnusedPortInRange(8080, 8100);
		System.out.println("found available port [" + port + "], setting up variable");		
		launchRepolets( port);
		
		ConfigurableClasspathResolverExternalSpace cfg = new ConfigurableClasspathResolverExternalSpace();
		
		cfg.setScopes( Scopes.compileScopes());
		cfg.setSkipOptional(false);
	
		
		if (settings != null) {		
			FakeMavenSettingsPersistenceExpertImpl persistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
			cfg.setOverrideSettingsPersistenceExpert(persistenceExpert);
		}
		
		if (localRepository != null) {
			FakeLocalRepositoryProvider localRepositoryProvider = new FakeLocalRepositoryProvider(localRepository);
			cfg.setOverrideLocalRepositoryExpert(localRepositoryProvider);
		}
		
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
		ove.addEnvironmentOverride("port", "" + port);
		cfg.setVirtualEnvironment(ove);
		
		
		WireContext<ClasspathResolverContract> context = ClasspathResolvers.classpathResolverContext( b -> {  
			b.bindContract(ClasspathResolverExternalContract.class, cfg);	
		});
		
		repoReflection = context.contract().repositoryReflection();
		
	}

	protected ArtifactInformation test(String solutionAsString, RetrievalMode mode) {
		Solution solution = NameParser.parseCondensedSolutionName( solutionAsString);
		ArtifactInformation information = repoReflection.retrieveInformation( solution, mode);
		return information;		
	}
	
	

	protected void dump(ArtifactInformation information) {
		if (information == null) {
			System.out.println("no information found");
			return;
		}
		
		System.out.print( "information : " + information.getGroupId() + ":" + information.getArtifactId() + "#" + information.getVersion() + "\n");
		LocalRepositoryInformation localInformation = information.getLocalInformation();
		if (localInformation != null) {
			System.out.println( "local information:");
			System.out.println("\t@ " + localInformation.getUrl());			
			for (PartInformation partInformation : localInformation.getPartInformation()) {
				String classifier = partInformation.getClassifier();
				if (classifier != null)
					System.out.print( "\t\t" + classifier + ":" + partInformation.getType() + "\t" + partInformation.getUrl() + "\n");
				else
					System.out.print( "\t\t" + partInformation.getType() + "\t" + partInformation.getUrl() + "\n");
			}
		}
		List<RemoteRepositoryInformation> remoteInformation = information.getRemoteInformation();
		System.out.println( "remote information:");
		for (RemoteRepositoryInformation remoteArtifactInformation : remoteInformation) {
			System.out.print( "\t" + remoteArtifactInformation.getName() + " @ " + remoteArtifactInformation.getUrl() + "\n");
			for (PartInformation partInformation : remoteArtifactInformation.getPartInformation()) {
				String classifier = partInformation.getClassifier();
				if (classifier != null)
					System.out.print( "\t\t" + classifier + ":" + partInformation.getType() + "\t" + partInformation.getUrl() + "\n");
				else
					System.out.print( "\t\t" + partInformation.getType() + "\t" + partInformation.getUrl() + "\n");
			}
		}
	}
	
	protected static void launchRepolets( int port) {
		String [] args = new String[1];
		args[0] = 	"archiveBase," + data[0].getAbsolutePath() + 
					";archiveComplement," + data[1].getAbsolutePath();				
		launcherShell = new LauncherShell( port);
		launcherShell.launch(args,  RepoType.singleZip);
	}
	
	@AfterClass
	public static void after() {	
		launcherShell.shutdown();
	}
	
	
	
}
