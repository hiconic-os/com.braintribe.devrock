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
