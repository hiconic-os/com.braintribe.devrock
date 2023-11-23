// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk.contract;

import java.io.File;

import com.braintribe.build.artifact.api.BuildRangeDependencyResolver;
import com.braintribe.build.artifact.api.DependencyResolver;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RepositoryConfigurationExposure;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.wire.api.space.WireSpace;

public interface BuildDependencyResolutionContract extends WireSpace {
	BuildRangeDependencyResolver buildDependencyResolver();
	ArtifactPomReader pomReader();
	File localRepository();
	DependencyResolver plainOptimisticDependencyResolver();
	MultiRepositorySolutionEnricher solutionEnricher();
	RepositoryReflection repositoryReflection();
	RepositoryConfigurationExposure repositoryConfigurationExposure();
}
