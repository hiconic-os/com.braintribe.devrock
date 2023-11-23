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
package com.braintribe.test.multi;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.cache.PartCacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricherFactory;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricherFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.enriching.RelevantPartTupleFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScopeImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.CrcValidationLevel;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactoryImpl;
import com.braintribe.build.artifact.walk.multi.ConfigurableWalker;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifact.walk.multi.WalkerFactory;
import com.braintribe.build.artifact.walk.multi.WalkerFactoryImpl;
import com.braintribe.build.artifact.walk.multi.clash.ClashResolverFactoryImpl;
import com.braintribe.build.artifact.walk.multi.exclusion.ExclusionControlFactory;
import com.braintribe.build.artifact.walk.multi.scope.ScopeControlFactory;
import com.braintribe.logging.LoggerInitializer;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDomain;
import com.braintribe.test.multi.realRepoWalk.ConfigurableDependencyResolverFactoryImpl;
import com.braintribe.test.multi.realRepoWalk.Monitor;
import com.braintribe.util.network.NetworkTools;

// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================
public abstract class AbstractWalkLab {
	

	protected static RepositoryReflectionImpl repositoryRegistry;
	protected static MavenSettingsReader mavenSettingsReader;	
	protected enum ScopeKind {compile, launch};	

	protected static MavenSettingsPersistenceExpert settingsPersistenceExpert;
	protected static LocalRepositoryLocationProvider localRepositoryLocationProvider;
	protected static RavenhurstScopeImpl scope;
	protected static OverrideableVirtualEnvironment virtualEnvironment;
	protected CrcValidationLevel crcValidationLevel = CrcValidationLevel.ignore;
	protected static ConfigurableDependencyResolverFactoryImpl resolverFactory;
	protected static MultiRepositorySolutionEnricherFactory enricherFactory;
	private static WalkerFactory walkerFactory;
	private static PomExpertFactory pomExpertFactory;
	protected static Monitor monitor;
	
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
		
		if (settingsPersistenceExpert != null) {
			mavenSettingsfactory.setSettingsPeristenceExpert( settingsPersistenceExpert);
		}
		if (localRepositoryLocationProvider != null) {
			mavenSettingsfactory.setInjectedRepositoryRetrievalExpert(localRepositoryLocationProvider);
		}
		
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
		
		
		monitor = new Monitor();
		monitor.setVerbosity( true);
		
		pomExpertFactory = new PomExpertFactory();
		//pomExpertFactory.addListener(monitor);
		pomExpertFactory.setCacheFactory( new CacheFactoryImpl());
		pomExpertFactory.setSettingsReader(mavenSettingsReader);
		
		resolverFactory = new ConfigurableDependencyResolverFactoryImpl(monitor);
		resolverFactory.setLocalRepositoryLocationProvider(mavenSettingsReader);
		resolverFactory.setPomExpertFactory(pomExpertFactory);
		resolverFactory.setRepositoryRegistry(repositoryRegistry);
		
		pomExpertFactory.setDependencyResolverFactory(resolverFactory);
		
		
		enricherFactory = new MultiRepositorySolutionEnricherFactoryImpl();
		enricherFactory.setRepositoryRegistry( repositoryRegistry);
		enricherFactory.setRelevantPartTupleFactory( new RelevantPartTupleFactoryImpl());
		enricherFactory.setPartCacheFactory( new PartCacheFactoryImpl());
		
		return port;
		
														
	}

	protected static void runAfter() {
		repositoryRegistry.closeContext();
	}
	

	
	protected static File buildWorkingCopyDirectory( String pathAsGroup) {
		String rootAsString = System.getenv( "BT__ARTIFACTS_HOME");
		if (rootAsString == null) {
			throw new IllegalArgumentException("Variable BT__ARTIFACTS_HOME must be set for the tests");
		}
		File root = new File(rootAsString);
		if (!root.exists()) {
			throw new IllegalArgumentException("The directory [" + root.getAbsolutePath() + "] must exist for the tests");
		}
		String path = pathAsGroup.replace( '.', File.separatorChar);
		return new File( root, path);				
	}
	
	

	
	/**
	 * build the {@link WalkFactory} 
	 * @param walkDomain - the {@link WalkDomain} sets what {@link DependencyResolver} to use 
	 * @return - the configured {@link WalkFactory}
	 */
	protected static WalkerFactory buildWalkFactory() {
	
		
		walkerFactory = new WalkerFactoryImpl();
		walkerFactory.setClashResolverFactory( new ClashResolverFactoryImpl());
		walkerFactory.setScopeControl( new ScopeControlFactory());
		walkerFactory.setExclusionControlFactory( new ExclusionControlFactory());
		walkerFactory.setMultiRepositoryDependencyResolverFactory( resolverFactory);
		walkerFactory.setSolutionEnricherFactory( enricherFactory);
		walkerFactory.setPomExpertFactory( pomExpertFactory);
		
		return walkerFactory;
	}	
	
	protected static Collection<Solution> test(String walkScopeId, Dependency terminal, WalkDenotationType denotationType, String [] expectedNames, int repeat, int threshold)  {
		Solution solution = resolveTerminalDependency(walkScopeId, terminal);
		return test( walkScopeId, solution, denotationType, expectedNames, repeat, threshold);
	}
	
	protected static Collection<Solution> test(String walkScopeId, Solution terminal, WalkDenotationType denotationType, String [] expectedNames, int repeat, int threshold)  {
		
			WalkerFactory walkFactory = buildWalkFactory();
			Walker walker = walkFactory.apply( denotationType);
			((ConfigurableWalker)walker).setAbortIfUnresolvedDependencyIsFound(false);

			return WalkingExpert.walk(walkScopeId, terminal, expectedNames, repeat, threshold, walker, true);		
	}
	
	protected static Collection<Solution> test(String walkScopeId, Dependency  terminal, WalkDenotationType denotationType, String [] expectedNames, int repeat, int threshold, boolean sort) throws Exception {
		Solution solution = resolveTerminalDependency(walkScopeId, terminal);
		return test( walkScopeId, solution, denotationType, expectedNames, repeat, threshold, sort);
	}
	
	protected static Collection<Solution> test(String walkScopeId, Solution terminal, WalkDenotationType denotationType, String [] expectedNames, int repeat, int threshold, boolean sort) throws Exception {
		
		WalkerFactory walkFactory = buildWalkFactory();
		Walker walker = walkFactory.apply( denotationType);
		((ConfigurableWalker)walker).setAbortIfUnresolvedDependencyIsFound(false);
		return WalkingExpert.walk(walkScopeId, terminal, expectedNames, repeat, threshold, walker, sort);		
}
	
	private static Solution resolveTerminalDependency(String walkScopeId, Dependency terminal) {
		DependencyResolver resolver = resolverFactory.get();
		Set<Solution> resolvedSolutions = resolver.resolveTopDependency(walkScopeId, terminal);
		if (resolvedSolutions == null || resolvedSolutions.size() == 0) {
			throw new IllegalStateException( "cannot find a solution for the dependency [" + NameParser.buildName(terminal));
		}
		List<Solution> solutions = resolvedSolutions.stream().collect( Collectors.toList());
		return solutions.get(0);
	}
	
	protected void testPresence( Collection<Solution> solutions, File repository) {
		WalkResultValidationExpert.testPresence(solutions, repository);
	}
	
	
	protected static void testUpdates( Collection<Solution> solutions, File repository) {
		
	}

}
