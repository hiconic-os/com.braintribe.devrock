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
import java.util.HashSet;
import java.util.Set;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScopeImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactoryImpl;
import com.braintribe.logging.LoggerInitializer;
import com.braintribe.util.network.NetworkTools;

public class AbstractRepositoryRegistryLab {
	protected static MavenSettingsReader mavenSettingsReader;		
	protected static MavenSettingsPersistenceExpert settingsPersistenceExpert;
	protected static LocalRepositoryLocationProvider localRepositoryLocationProvider;
	protected static RavenhurstScopeImpl scope;
	protected static RepositoryReflectionImpl repositoryRegistry;

	protected static int runBefore() {
		LoggerInitializer loggerInitializer = new LoggerInitializer();
		try {					
			loggerInitializer.setLoggerConfigUrl( new File("res/logger.properties").toURI().toURL());		
			loggerInitializer.afterPropertiesSet();
		} catch (Exception e) {		
			e.printStackTrace();
		}
		
		int port = NetworkTools.getUnusedPortInRange(8080, 8100);
		MavenSettingsExpertFactory mavenSettingsfactory = new MavenSettingsExpertFactory();
		if (settingsPersistenceExpert != null) {
			mavenSettingsfactory.setSettingsPeristenceExpert( settingsPersistenceExpert);
		}
		if (localRepositoryLocationProvider != null) {
			mavenSettingsfactory.setInjectedRepositoryRetrievalExpert(localRepositoryLocationProvider);
		}
		
		scope = new RavenhurstScopeImpl();
		mavenSettingsReader = mavenSettingsfactory.getMavenSettingsReader();
		OverrideableVirtualEnvironment ve = new OverrideableVirtualEnvironment();
		ve.addEnvironmentOverride( "port", ""+ port);
		mavenSettingsReader.setVirtualEnvironment(ve);
		
		scope.setReader(mavenSettingsReader);
		
		
		Set<String> inhibitList = new HashSet<String>();
		scope.setInhibitedRepositoryIds(inhibitList);		
														
		repositoryRegistry = new RepositoryReflectionImpl();
		repositoryRegistry.setMavenSettingsReader( mavenSettingsReader);
		repositoryRegistry.setLocalRepositoryLocationProvider(mavenSettingsReader);
		repositoryRegistry.setRavenhurstScope(scope);
		repositoryRegistry.setArtifactFilterExpertSupplier( scope);
		repositoryRegistry.setInterrogationClientFactory( new RepositoryInterrogationClientFactoryImpl());
		repositoryRegistry.setAccessClientFactory( new RepositoryAccessClientFactoryImpl());
		
		return port;
	}

	protected static void runAfter() {
		
	}
}
