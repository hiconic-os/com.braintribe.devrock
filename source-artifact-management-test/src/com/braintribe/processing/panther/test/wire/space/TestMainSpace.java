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
package com.braintribe.processing.panther.test.wire.space;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScopeImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolverFactory;
import com.braintribe.build.artifact.retrieval.multi.resolving.MultiRepositoryDependencyResolverImpl;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactoryImpl;
import com.braintribe.model.processing.panther.depmgt.PublishWalkDependencyResolver;
import com.braintribe.processing.panther.test.wire.contract.ExternalConfigurationContract;
import com.braintribe.processing.panther.test.wire.contract.TestMainContract;
import com.braintribe.wire.api.annotation.Bean;
import com.braintribe.wire.api.annotation.Beans;
import com.braintribe.wire.api.annotation.Import;

@Beans
public class TestMainSpace implements TestMainContract {
	
	@Import
	private ExternalConfigurationContract externalConfiguration;
	
	public ArtifactPomReader pomReader() {
		ArtifactPomReader bean = pomExpertFactory().getReader();
		bean.setEnforceParentResolving(true);
		return bean;
	}

	@Bean
	public MavenSettingsExpertFactory settingsExpertFactory() {
		MavenSettingsExpertFactory bean = new MavenSettingsExpertFactory();
		return bean;
	}
	
	@Bean 
	@Override
	public PublishWalkDependencyResolver dependencyResolver() {
		PublishWalkDependencyResolver bean = new PublishWalkDependencyResolver();
		
		bean.setArtifactsToBePublished(externalConfiguration.sourceArtifacts());
		bean.setSvnWalkCache(externalConfiguration.walkCache());
		bean.setDelegate(standardDependencyResolver());
		
		return bean;
	}
	
	// to be used without publish walk dependency resolver
	@Bean 
	public DependencyResolverFactory standardDependencyResolverFactory() {
		DependencyResolverFactory bean = new DependencyResolverFactory() {
						
			@Override
			public DependencyResolver get() {			
				return standardDependencyResolver();
			}
		};
		return bean;
	}
	
	@Bean
	public DependencyResolver standardDependencyResolver() {
		MultiRepositoryDependencyResolverImpl bean = new MultiRepositoryDependencyResolverImpl();
		
		bean.setRepositoryRegistry(repositoryReflection());
		bean.setPomExpertFactory(pomExpertFactory());
		
		return bean;
	}
	
	@Bean 
	public PomExpertFactory pomExpertFactory() {
		PomExpertFactory bean = new PomExpertFactory();
		
		bean.setSettingsReader(settingsReader());
		bean.setDependencyResolverFactory(dependencyResolver());
		bean.setCacheFactory(cacheFactory());
		
		return bean;
	}
	
	@Bean
	public CacheFactoryImpl cacheFactory() {
		CacheFactoryImpl bean = new CacheFactoryImpl();
		return bean;
	}
	
	@Bean
	public RepositoryReflectionImpl repositoryReflection() {
		RepositoryReflectionImpl bean = new RepositoryReflectionImpl();
		
		bean.setInterrogationClientFactory(repositoryInterrogationClientFactory());
		bean.setAccessClientFactory(repositoryAccessClientFactory());
		bean.setRavenhurstScope(ravenhurstScope());
		bean.setMavenSettingsReader(settingsReader());
		
		return bean;
	}
	
	@Bean 
	public RepositoryInterrogationClientFactoryImpl repositoryInterrogationClientFactory() {
		RepositoryInterrogationClientFactoryImpl bean = new RepositoryInterrogationClientFactoryImpl();
		return bean;
	}
	
	@Bean 
	public RepositoryAccessClientFactoryImpl repositoryAccessClientFactory() {
		RepositoryAccessClientFactoryImpl bean = new RepositoryAccessClientFactoryImpl();
		return bean;
	}
	
	@Bean
	public MavenSettingsReader settingsReader() {
		MavenSettingsReader bean = settingsExpertFactory().getMavenSettingsReader();
		return bean;
	}
	
	@Bean
	public RavenhurstScopeImpl ravenhurstScope() {
		RavenhurstScopeImpl bean = new RavenhurstScopeImpl();
		bean.setReader(settingsReader());
		return bean;
	}
	
}
