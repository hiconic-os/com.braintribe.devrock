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
package com.braintribe.artifacts.codebase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.cache.PartCacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricherFactory;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricherFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.enriching.RelevantPartTupleFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScopeImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.CrcValidationLevel;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolverFactory;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactoryImpl;
import com.braintribe.build.artifact.walk.multi.WalkerFactory;
import com.braintribe.build.artifact.walk.multi.WalkerFactoryImpl;
import com.braintribe.build.artifact.walk.multi.clash.ClashResolverFactoryImpl;
import com.braintribe.build.artifact.walk.multi.exclusion.ExclusionControlFactory;
import com.braintribe.build.artifact.walk.multi.scope.ScopeControlFactory;
import com.braintribe.logging.LoggerInitializer;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class AbstractCodebaseAwareLab {
	protected static final String EXPANDED_GROUP = "${groupId.expanded}/${version}/${artifactId}";
	protected static final String FLATTENED_GROUP = "${groupId}/${version}/${artifactId}";
	protected static final String EXPANDED_STANDARD = "${groupId.expanded}/${artifactId}/${version}";
	protected static final String FLATTENED_STANDARD = "${groupId}/${artifactId}/${version}";
	
	protected static RepositoryReflectionImpl repositoryRegistry;
	protected static MavenSettingsReader mavenSettingsReader;		
	protected static MavenSettingsPersistenceExpert settingsPersistenceExpert;
	protected static LocalRepositoryLocationProvider localRepositoryLocationProvider;
	protected static RavenhurstScopeImpl scope;
	protected CrcValidationLevel crcValidationLevel = CrcValidationLevel.ignore;
	protected static PomExpertFactory pomExpertFactory;
	protected static AgnosticDependencyResolverFactoryImpl agnosticResolverFactory;
	protected static CodebaseAwareDependencyResolverFactoryImpl groupingAwareResolverFactory;
	protected static File contents = new File("res/grouping");
	protected static File masterCodebase = new File( contents, "grouping.flattened");
	
	protected static File target;
	
	protected static void runBefore() {
		runBefore(CrcValidationLevel.ignore);
	}
	protected static void runBefore(CrcValidationLevel crcValidationLevel) {
		
		LoggerInitializer loggerInitializer = new LoggerInitializer();
		try {					
			loggerInitializer.setLoggerConfigUrl( new File("res/logger.properties").toURI().toURL());		
			loggerInitializer.afterPropertiesSet();
		} catch (Exception e) {		
			e.printStackTrace();
		}
		
		MavenSettingsExpertFactory mavenSettingsfactory = new MavenSettingsExpertFactory();
		if (settingsPersistenceExpert != null) {
			mavenSettingsfactory.setSettingsPeristenceExpert( settingsPersistenceExpert);
		}
		if (localRepositoryLocationProvider != null) {
			mavenSettingsfactory.setInjectedRepositoryRetrievalExpert(localRepositoryLocationProvider);
		}
		
		scope = new RavenhurstScopeImpl();
		mavenSettingsReader = mavenSettingsfactory.getMavenSettingsReader();
		scope.setReader(mavenSettingsReader);
		
		Set<String> inhibitList = new HashSet<String>();
		scope.setInhibitedRepositoryIds(inhibitList);
		
		repositoryRegistry = new RepositoryReflectionImpl();
		repositoryRegistry.setInterrogationClientFactory( new RepositoryInterrogationClientFactoryImpl());				
		repositoryRegistry.setAccessClientFactory( new RepositoryAccessClientFactoryImpl());
		repositoryRegistry.setRavenhurstScope(scope);
		repositoryRegistry.setMavenSettingsReader(mavenSettingsReader);
		repositoryRegistry.setArtifactFilterExpertSupplier( scope);
		repositoryRegistry.setLocalRepositoryLocationProvider(mavenSettingsReader);
		repositoryRegistry.setCrcValidationLevel( crcValidationLevel);
		
		
		pomExpertFactory = new PomExpertFactory();
		//pomExpertFactory.addListener(monitor);
		pomExpertFactory.setCacheFactory( new CacheFactoryImpl());
		pomExpertFactory.setSettingsReader( mavenSettingsReader);
		
		agnosticResolverFactory = new AgnosticDependencyResolverFactoryImpl( null);
		agnosticResolverFactory.setLocalRepositoryLocationProvider( mavenSettingsReader);
		agnosticResolverFactory.setPomExpertFactory( pomExpertFactory);
		agnosticResolverFactory.setRepositoryRegistry( repositoryRegistry);
		
		groupingAwareResolverFactory = new CodebaseAwareDependencyResolverFactoryImpl( null);
		groupingAwareResolverFactory.setLocalRepositoryLocationProvider( mavenSettingsReader);
		groupingAwareResolverFactory.setPomExpertFactory( pomExpertFactory);
		groupingAwareResolverFactory.setRepositoryRegistry( repositoryRegistry);
		
														
	}
	
	protected static MultiRepositorySolutionEnricherFactory enricherFactory() {
		MultiRepositorySolutionEnricherFactory enricherFactory = new MultiRepositorySolutionEnricherFactoryImpl();
		enricherFactory.setRepositoryRegistry( repositoryRegistry);
		enricherFactory.setRelevantPartTupleFactory( new RelevantPartTupleFactoryImpl());
		enricherFactory.setPartCacheFactory( new PartCacheFactoryImpl());
		return enricherFactory;
	}
	
	protected static WalkerFactory walkerFactory(DependencyResolverFactory resolverFactory) {
		
		
		WalkerFactory walkerFactory = new WalkerFactoryImpl();
		walkerFactory.setClashResolverFactory( new ClashResolverFactoryImpl());
		walkerFactory.setScopeControl( new ScopeControlFactory());
		walkerFactory.setExclusionControlFactory( new ExclusionControlFactory());
		walkerFactory.setMultiRepositoryDependencyResolverFactory( resolverFactory);
		walkerFactory.setSolutionEnricherFactory( enricherFactory());
		walkerFactory.setPomExpertFactory( pomExpertFactory);
		
		return walkerFactory;
	}
	
	
	protected void assertSolution( Solution solution, String grp, String artifact, String version, String ... deps) {
		Assert.assertTrue("groupId [" + grp + "] expected, [" + solution.getGroupId() + "] found", solution.getGroupId().equalsIgnoreCase(grp));
		Assert.assertTrue("artifactId [" + artifact + "] expected, [" + solution.getArtifactId() + "] found", solution.getArtifactId().equalsIgnoreCase(artifact));
		String sVersion = VersionProcessor.toString(solution.getVersion());
		Assert.assertTrue("version [" + version + "] expected, [" + sVersion + "] found", sVersion.equalsIgnoreCase( version));
		
		if (deps != null) {
			List<Dependency> dependencies = solution.getDependencies();
			//Assert.assertTrue("expected [" + deps.length + "] dependencies, found [" + dependencies.size() + "]", deps.length == dependencies.size());
			List<String> foundNames = dependencies.stream().map( d -> NameParser.buildName(d)).collect( Collectors.toList());
		
			List<String> found = new ArrayList<String>();
			List<String> notFound = new ArrayList<String>();
			for (String dep : deps) {
				if (foundNames.contains( dep)) {
					found.add(dep);
				}
				else {
					notFound.add(dep);
				}
			}
			if (notFound.size() > 0) {
				for (String nf : notFound) {
					System.out.println( nf);
				}
				Assert.fail("[" + notFound.size() + "] expected dependencies were not found");
			}
			if (found.size() != foundNames.size()) {
				foundNames.removeAll(found);
				for (String f : foundNames) {
					System.out.println(f);
				}
				Assert.fail("[" + foundNames.size() + "] dependencies were not expected");
			}
		}
		
	}
	

	protected static void runAfter() {
		repositoryRegistry.closeContext();
	
	}
}
