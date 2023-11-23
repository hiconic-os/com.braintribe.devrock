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
package com.braintribe.artifacts.test.maven.settings.environment;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScopeImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.CrcValidationLevel;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactoryImpl;
import com.braintribe.logging.LoggerInitializer;
import com.braintribe.util.network.NetworkTools;

public abstract class AbstractSettingsEnviromentLab {
	
	protected static RepositoryReflectionImpl repositoryRegistry;
	protected static MavenSettingsReader mavenSettingsReader;	
	protected enum ScopeKind {compile, launch};	

	protected static MavenSettingsPersistenceExpert settingsPersistenceExpert;
	protected static LocalRepositoryLocationProvider localRepositoryLocationProvider;
	protected static RavenhurstScopeImpl scope;
	protected static OverrideableVirtualEnvironment virtualEnvironment;
	protected CrcValidationLevel crcValidationLevel = CrcValidationLevel.ignore;

	
	protected static int runBefore() {
		return runBefore(CrcValidationLevel.ignore);
	}
	protected static int runBefore(CrcValidationLevel crcValidationLevel) {
		LoggerInitializer loggerInitializer = new LoggerInitializer();
		try {					
			loggerInitializer.setLoggerConfigUrl( new File("res/logger.properties").toURI().toURL());		
			loggerInitializer.afterPropertiesSet();
		} catch (Exception e) {		
			e.printStackTrace();
		}
		
		virtualEnvironment = new OverrideableVirtualEnvironment();
		
		
		MavenSettingsExpertFactory mavenSettingsfactory = new MavenSettingsExpertFactory();
		mavenSettingsfactory.setVirtualEnvironment(virtualEnvironment);
				
		
		int port = NetworkTools.getUnusedPortInRange(8080, 8100);
		System.out.println("found available port [" + port + "], setting up variable");
		virtualEnvironment.addEnvironmentOverride("port", "" + port);
		
		scope = new RavenhurstScopeImpl();
		mavenSettingsReader = mavenSettingsfactory.getMavenSettingsReader();
		scope.setReader(mavenSettingsReader);
		scope.setVirtualEnvironment(virtualEnvironment);
		mavenSettingsReader.setVirtualEnvironment(virtualEnvironment);
		
		Set<String> inhibitList = new HashSet<String>();
		scope.setInhibitedRepositoryIds(inhibitList);
		
		repositoryRegistry = new RepositoryReflectionImpl();
		repositoryRegistry.setInterrogationClientFactory( new RepositoryInterrogationClientFactoryImpl());				
		repositoryRegistry.setAccessClientFactory( new RepositoryAccessClientFactoryImpl());
		repositoryRegistry.setRavenhurstScope(scope);
		repositoryRegistry.setMavenSettingsReader(mavenSettingsReader);
		repositoryRegistry.setLocalRepositoryLocationProvider(mavenSettingsReader);
		repositoryRegistry.setArtifactFilterExpertSupplier( scope);
		repositoryRegistry.setCrcValidationLevel( crcValidationLevel);
		repositoryRegistry.setHysteresis(1);
		
		return port;		
														
	}

	protected static void runAfter() {
		repositoryRegistry.closeContext();
	}
}
