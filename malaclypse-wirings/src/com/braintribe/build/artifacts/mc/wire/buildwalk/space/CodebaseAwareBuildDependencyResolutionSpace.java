// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk.space;

import java.io.File;

import com.braintribe.build.artifact.api.BuildRangeDependencyResolver;
import com.braintribe.build.artifact.api.DependencyResolver;
import com.braintribe.build.artifact.codebase.reflection.CodebaseReflection;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RepositoryConfigurationExposure;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.CodebaseAwareBuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.CodebaseConfigurationContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class CodebaseAwareBuildDependencyResolutionSpace implements CodebaseAwareBuildDependencyResolutionContract {

	@Import
	private BuildDependencyResolutionContract buildDependencyResolution;
	
	@Import
	private CodebaseConfigurationContract codebaseConfiguration;
	
	@Import
	private CodebaseAwareBuildDependencyResolutionConfigurationSpace resolutionConfiguration; 
	
	@Override
	public BuildRangeDependencyResolver buildDependencyResolver() {
		return buildDependencyResolution.buildDependencyResolver();
	}

	@Override
	public CodebaseReflection codebaseReflection() {
		return resolutionConfiguration.codebaseReflection();
	}
	
	@Override
	public ArtifactPomReader pomReader() {
		return buildDependencyResolution.pomReader();
	}
	
	@Override
	public File localRepository() {
		return buildDependencyResolution.localRepository();
	}

	@Override
	public DependencyResolver plainOptimisticDependencyResolver() {
		return buildDependencyResolution.plainOptimisticDependencyResolver();
	}
	
	@Override
	public MultiRepositorySolutionEnricher solutionEnricher() {
		return buildDependencyResolution.solutionEnricher();
	}

	@Override
	public RepositoryReflection repositoryReflection() {	
		return buildDependencyResolution.repositoryReflection();
	}

	@Override
	public RepositoryConfigurationExposure repositoryConfigurationExposure() {		
		return buildDependencyResolution.repositoryConfigurationExposure();
	}
	
	

}
