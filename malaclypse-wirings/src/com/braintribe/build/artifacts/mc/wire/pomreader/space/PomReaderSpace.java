// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.pomreader.space;

import java.util.function.Function;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScopeImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.MultiRepositoryDependencyResolverImpl;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactoryImpl;
import com.braintribe.build.artifacts.mc.wire.pomreader.contract.PomReaderContract;
import com.braintribe.build.artifacts.mc.wire.pomreader.external.contract.PomReaderExternalContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;

@Managed
public class PomReaderSpace implements PomReaderContract {
	
	@Import
	private PomReaderExternalContract externalConfiguration;
	
	@Override
	public ArtifactPomReader pomReader() {
		ArtifactPomReader bean = pomExpertFactory().getReader();
		return bean;
	}
	
	@Override
	@Managed
	public ArtifactPomReader leanPomReader() {
		ArtifactPomReader bean = leanPomExpertFactory().getReader();
		return bean;
	}
	
	@Managed
	public MavenSettingsExpertFactory settingsExpertFactory() {
		MavenSettingsExpertFactory bean = new MavenSettingsExpertFactory();
		bean.setVirtualEnvironment(externalConfiguration.virtualEnvironment());
		
		return bean;
	}
	
	@Managed
	public DependencyResolver dependencyResolver() {
		Function<DependencyResolver, DependencyResolver> topDependencyResolver = externalConfiguration.dependencyResolverEnricher();
		
		if (topDependencyResolver == null)
			return standardDependencyResolver();
		else
			return topDependencyResolver.apply(standardDependencyResolver());
	}
	
	@Managed
	@Override
	public DependencyResolver standardDependencyResolver() {
		MultiRepositoryDependencyResolverImpl bean = new MultiRepositoryDependencyResolverImpl();
		
		bean.setRepositoryRegistry(repositoryReflection());
		bean.setPomExpertFactory(pomExpertFactory());
		
		return bean;
	}
	
	@Managed 
	public PomExpertFactory pomExpertFactory() {
		PomExpertFactory bean = new PomExpertFactory();

		bean.setSettingsReader(settingsReader());
		bean.setVirtualEnvironment(externalConfiguration.virtualEnvironment());
		bean.setDependencyResolverFactory(this::dependencyResolver);
		bean.setCacheFactory(cacheFactory());
		bean.setEnforceParentResolving(true);
		
		return bean;

	}
	
	@Managed 
	public PomExpertFactory leanPomExpertFactory() {
		PomExpertFactory bean = new PomExpertFactory();
		
		bean.setSettingsReader(settingsReader());
		bean.setVirtualEnvironment(externalConfiguration.virtualEnvironment());
		bean.setDependencyResolverFactory(this::dependencyResolver);
		bean.setCacheFactory(cacheFactory());
		bean.setEnforceParentResolving(false);
		bean.setIdentifyArtifactOnly(true);
		
		return bean;
		
	}
	
	@Managed
	public CacheFactoryImpl cacheFactory() {
		CacheFactoryImpl bean = new CacheFactoryImpl();
		return bean;
	}
	
	@Managed
	public RepositoryReflectionImpl repositoryReflection() {
		RepositoryReflectionImpl bean = new RepositoryReflectionImpl();
		
		bean.setInterrogationClientFactory(repositoryInterrogationClientFactory());
		bean.setAccessClientFactory(repositoryAccessClientFactory());
		bean.setRavenhurstScope(ravenhurstScope());
		bean.setMavenSettingsReader(settingsReader());
		bean.setLocalRepositoryLocationProvider(settingsReader());
		bean.setArtifactFilterExpertSupplier(ravenhurstScope());
		bean.setCurrentScopeId( externalConfiguration.globalMalaclypseScopeId());
		
		InstanceConfiguration.currentInstance().onDestroy(bean::closeContext);
		
		return bean;
	}
	
	@Managed 
	public RepositoryInterrogationClientFactoryImpl repositoryInterrogationClientFactory() {
		RepositoryInterrogationClientFactoryImpl bean = new RepositoryInterrogationClientFactoryImpl();
		return bean;
	}
	
	
	
	@Managed 
	public RepositoryAccessClientFactoryImpl repositoryAccessClientFactory() {
		RepositoryAccessClientFactoryImpl bean = new RepositoryAccessClientFactoryImpl();
		return bean;
	}
	
	@Managed
	@Override
	public MavenSettingsReader settingsReader() {
		
		MavenSettingsReader bean = settingsExpertFactory().getMavenSettingsReader();	
		return bean;
	}
	
	@Managed
	public RavenhurstScopeImpl ravenhurstScope() {
		RavenhurstScopeImpl bean = new RavenhurstScopeImpl();
		bean.setVirtualEnvironment(externalConfiguration.virtualEnvironment());
		bean.setReader(settingsReader());		
		return bean;
	}
	
}
