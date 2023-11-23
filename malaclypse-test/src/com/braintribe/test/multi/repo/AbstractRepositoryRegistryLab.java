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
