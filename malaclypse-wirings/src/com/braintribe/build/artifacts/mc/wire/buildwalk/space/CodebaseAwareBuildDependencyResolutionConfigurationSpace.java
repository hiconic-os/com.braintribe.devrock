// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk.space;

import com.braintribe.build.artifact.codebase.reflection.TemplateBasedCodebaseReflection;
import com.braintribe.build.artifact.retrieval.multi.resolving.CodebaseAwareDependencyResolver;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.CodebaseConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.IntransitiveResolutionContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class CodebaseAwareBuildDependencyResolutionConfigurationSpace implements IntransitiveResolutionContract {
	@Import
	private BuildDependencyResolutionSpace buildDependencyResolutionSpace;
	
	@Import
	private CodebaseConfigurationContract codebaseConfiguration;
	
	@Override
	@Managed 
	public CodebaseAwareDependencyResolver intransitiveDependencyResolver() {
		CodebaseAwareDependencyResolver bean = new CodebaseAwareDependencyResolver();
		bean.setDelegate(buildDependencyResolutionSpace.standardDependencyResolver());
		bean.setCodebaseReflection(codebaseReflection());
		bean.setPomExpertFactory(buildDependencyResolutionSpace.leanPomExpertFactory());
		return bean;
	}
	
	@Managed
	public TemplateBasedCodebaseReflection codebaseReflection() {
		TemplateBasedCodebaseReflection bean = new TemplateBasedCodebaseReflection(codebaseConfiguration.codebaseRoot(), codebaseConfiguration.codebasePattern());
		bean.setDefaultVersion( codebaseConfiguration.defaultVersion());
		return bean;
	}
}
