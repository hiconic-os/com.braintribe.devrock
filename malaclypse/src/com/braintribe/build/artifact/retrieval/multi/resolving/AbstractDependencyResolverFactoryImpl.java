// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;

import java.util.HashSet;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationBroadcaster;
import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationListener;

public abstract class AbstractDependencyResolverFactoryImpl implements ConfigurableDependencyResolverFactory, DependencyResolverNotificationBroadcaster {

	protected HashSet<DependencyResolverNotificationListener> listeners;
	protected RepositoryReflection repositoryRegistry;
	protected PomExpertFactory pomExpertFactory;
	protected LocalRepositoryLocationProvider localRepositoryLocationProvider;
	
	@Override
	public void addListener(DependencyResolverNotificationListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(DependencyResolverNotificationListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void setRepositoryRegistry(RepositoryReflection registry) {
		this.repositoryRegistry = registry;
	}

	@Override
	public void setPomExpertFactory(PomExpertFactory factory) {
		this.pomExpertFactory = factory;
	}

	@Override
	public void setLocalRepositoryLocationProvider(LocalRepositoryLocationProvider provider) {
		this.localRepositoryLocationProvider = provider;
	}
	
	

}
