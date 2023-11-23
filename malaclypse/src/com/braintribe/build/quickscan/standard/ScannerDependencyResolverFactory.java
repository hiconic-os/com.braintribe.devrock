// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.quickscan.standard;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.resolving.ConfigurableDependencyResolverFactory;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolverFactory;
import com.braintribe.build.artifact.retrieval.multi.resolving.LocalRepositoryDependencyResolverImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.WorkingCopyDependencyResolverImpl;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;


public class ScannerDependencyResolverFactory implements DependencyResolverFactory, ConfigurableDependencyResolverFactory {

	private PomExpertFactory pomExpertFactory;
	private LocalRepositoryLocationProvider localRepoLocationProvider;
	private LocalRepositoryLocationProvider workingCopyLocationProvider;

	@Configurable @Required
	public void setWorkingCopyLocationProvider(LocalRepositoryLocationProvider locationProvider) {
		this.workingCopyLocationProvider = locationProvider;
	}
	
	public ScannerDependencyResolverFactory() {
	
	}
	
	@Override
	public DependencyResolver get() throws RuntimeException {
		LocalRepositoryDependencyResolverImpl localResolver = new LocalRepositoryDependencyResolverImpl();
		localResolver.setLocalRepositoryLocationProvider(localRepoLocationProvider);
		localResolver.setPomExpertFactory(pomExpertFactory);

		WorkingCopyDependencyResolverImpl workingCopyresolver = new WorkingCopyDependencyResolverImpl();
		workingCopyresolver.setPomExpertFactory( pomExpertFactory);				
		workingCopyresolver.setLocalRepositoryLocationProvider( workingCopyLocationProvider);
		workingCopyresolver.setDelegate(localResolver);
		workingCopyresolver.setDelegateLocationExpert(localRepoLocationProvider);
		
		return workingCopyresolver;
	}

	@Override
	public void setRepositoryRegistry(RepositoryReflection registry) {
	}

	@Override
	public void setPomExpertFactory(PomExpertFactory factory) {
		this.pomExpertFactory = factory;
	}

	@Override
	public void setLocalRepositoryLocationProvider(LocalRepositoryLocationProvider provider) {
		localRepoLocationProvider = provider;
	}
	
	

}
