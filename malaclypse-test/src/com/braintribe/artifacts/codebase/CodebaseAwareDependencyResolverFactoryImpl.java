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
import java.net.MalformedURLException;
import java.net.URL;

import com.braintribe.build.artifact.codebase.reflection.CodebaseReflection;
import com.braintribe.build.artifact.codebase.reflection.TemplateBasedCodebaseReflection;
import com.braintribe.build.artifact.retrieval.multi.resolving.AbstractDependencyResolverFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.CodebaseAwareDependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.MultiRepositoryDependencyResolverImpl;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.test.multi.realRepoWalk.Monitor;

public class CodebaseAwareDependencyResolverFactoryImpl extends AbstractDependencyResolverFactoryImpl{
	
	private Monitor monitor;
	private DependencyResolver resolver;
	private SourceRepository sourceRepository;
	private String templateStr; //= "${groupId}/${version}/${artifactId}";
	
	public CodebaseAwareDependencyResolverFactoryImpl( Monitor monitor) {
	
		this.monitor = monitor;
	}
	
	@Configurable @Required
	public void setSourceRepository(SourceRepository sourceRepository) {
		this.sourceRepository = sourceRepository;
	}
	
	@Configurable @Required
	public void setCodebaseTemplate( String template) {
		templateStr = template;
	}
	
	@Override
	public DependencyResolver get() throws RuntimeException {
		if (resolver != null)
			return resolver;

		MultiRepositoryDependencyResolverImpl remoteResolver = new MultiRepositoryDependencyResolverImpl();						
		remoteResolver.setRepositoryRegistry(repositoryRegistry);
		remoteResolver.setPomExpertFactory(pomExpertFactory);
		remoteResolver.addListener( monitor);
			
		String repoUrlAsString = sourceRepository.getRepoUrl();
		URL repoUrl;
		try {
			repoUrl = new URL( repoUrlAsString);
		} catch (MalformedURLException e) {
			throw new RuntimeException( e);
		}
		File repoRoot = new File ( repoUrl.getFile());
		
		if (templateStr == null) {
			throw new RuntimeException("no template passed");
		}
		
		CodebaseReflection codebaseReflection = new TemplateBasedCodebaseReflection( repoRoot, templateStr);
	 
		CodebaseAwareDependencyResolver wcResolver = new CodebaseAwareDependencyResolver();
		wcResolver.setCodebaseReflection(codebaseReflection);
		wcResolver.setPomExpertFactory(pomExpertFactory);
		
		wcResolver.setDelegate(remoteResolver);
			 
		resolver = wcResolver;
		resolver.addListener( monitor);
			
		return resolver;
	}


}
