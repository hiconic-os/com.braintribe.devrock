// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;

public interface ConfigurableDependencyResolverFactory extends DependencyResolverFactory{
	void setRepositoryRegistry( RepositoryReflection registry);
	void setPomExpertFactory( PomExpertFactory factory);
	void setLocalRepositoryLocationProvider( LocalRepositoryLocationProvider provider);
}
