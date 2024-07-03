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
