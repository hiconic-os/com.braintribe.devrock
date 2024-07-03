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
package com.braintribe.artifacts.codebase;

import java.util.List;

import com.braintribe.build.artifact.retrieval.multi.resolving.AbstractDependencyResolverFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.MultiRepositoryDependencyResolverImpl;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.test.multi.realRepoWalk.Monitor;

public class AgnosticDependencyResolverFactoryImpl extends AbstractDependencyResolverFactoryImpl{
	
	private Monitor monitor;
	private DependencyResolver resolver;
	private List<SourceRepository> sourceRepositories;
	
	public AgnosticDependencyResolverFactoryImpl( Monitor monitor) {
	
		this.monitor = monitor;
	}
	
	public void setSourceRepositories(List<SourceRepository> sourceRepositories) {
		this.sourceRepositories = sourceRepositories;
	}
	
	@Override
	public DependencyResolver get() throws RuntimeException {
		if (resolver != null)
			return resolver;

		MultiRepositoryDependencyResolverImpl remoteResolver = new MultiRepositoryDependencyResolverImpl();						
		remoteResolver.setRepositoryRegistry(repositoryRegistry);
		remoteResolver.setPomExpertFactory(pomExpertFactory);
		remoteResolver.addListener( monitor);
			
	 
		AgnosticDependencyResolverImpl wcResolver = new AgnosticDependencyResolverImpl();
		wcResolver.setSourceRepositories( sourceRepositories);
					 
		wcResolver.setDelegate(remoteResolver);
			 
		resolver = wcResolver;
		resolver.addListener( monitor);
			
		return resolver;
	}


}
