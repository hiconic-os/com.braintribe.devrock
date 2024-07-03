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
package com.braintribe.artifacts.test.maven.pom;

import java.io.File;
import java.util.UUID;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScopeImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactoryImpl;
import com.braintribe.model.artifact.Solution;
import com.braintribe.test.multi.realRepoWalk.ConfigurableDependencyResolverFactoryImpl;

public abstract class AbstractPomReaderLab {
	protected static MavenSettingsPersistenceExpert settingsPersistenceExpert;
	protected static LocalRepositoryLocationProvider localRepositoryLocationProvider;
	protected static File contents = new File( "res/maven/reader/contents");
	protected static File settings = new File( contents, "settings.xml");
	protected static File localRepository = new File ( contents, "repo");
	protected static MavenSettingsReader mavenSettingsReader;
	protected static PomExpertFactory pomExpertFactory;
	protected static RepositoryReflectionImpl repositoryRegistry;
	protected static RavenhurstScopeImpl scope;
	
	protected static void runbefore  () {
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		
		
		localRepository.mkdirs();
		runbefore( settingsPersistenceExpert, localRepositoryLocationProvider);
			
	}
	
	protected static void runbefore  (MavenSettingsPersistenceExpert pExpert, LocalRepositoryLocationProvider lrProvider) {
	
		
		MavenSettingsExpertFactory mavenSettingsfactory = new MavenSettingsExpertFactory();
		if (pExpert != null) {
			mavenSettingsfactory.setSettingsPeristenceExpert( pExpert);
		}
		if (lrProvider != null) {
			mavenSettingsfactory.setInjectedRepositoryRetrievalExpert(lrProvider);
		}
		mavenSettingsReader = mavenSettingsfactory.getMavenSettingsReader();
		scope = new RavenhurstScopeImpl();
		scope.setReader(mavenSettingsReader);
		
		repositoryRegistry = new RepositoryReflectionImpl();
		repositoryRegistry.setInterrogationClientFactory( new RepositoryInterrogationClientFactoryImpl());				
		repositoryRegistry.setAccessClientFactory( new RepositoryAccessClientFactoryImpl());
		repositoryRegistry.setRavenhurstScope(scope);
		repositoryRegistry.setMavenSettingsReader(mavenSettingsReader);
		repositoryRegistry.setArtifactFilterExpertSupplier( scope);
		repositoryRegistry.setLocalRepositoryLocationProvider(mavenSettingsReader);
		
		ConfigurableDependencyResolverFactoryImpl resolverFactory = new ConfigurableDependencyResolverFactoryImpl( null);
		resolverFactory.setLocalRepositoryLocationProvider(mavenSettingsReader);
		resolverFactory.setRepositoryRegistry(repositoryRegistry);
				
		pomExpertFactory = new PomExpertFactory();
		pomExpertFactory.setSettingsReader(mavenSettingsReader);
		pomExpertFactory.setDependencyResolverFactory(resolverFactory);
		pomExpertFactory.setCacheFactory( new CacheFactoryImpl());
				
		resolverFactory.setPomExpertFactory(pomExpertFactory);
	}
	protected Solution readPom( File testPomFile) {
		ArtifactPomReader reader = pomExpertFactory.getReader();
		reader.setExpansionLeniency(false);
		Solution solution;
		try {
			solution = reader.readPom( UUID.randomUUID().toString(), testPomFile);
			return solution;
		} catch (PomReaderException e) {
			//Assert.fail("exception thrown :" + e.getMessage());
			return null;
		}
	}
}
