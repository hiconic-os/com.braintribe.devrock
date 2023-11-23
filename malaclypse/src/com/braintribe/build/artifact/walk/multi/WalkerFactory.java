// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi;

import java.util.function.Function;

import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricherFactory;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolverFactory;
import com.braintribe.build.artifact.walk.multi.clash.ClashResolverFactory;
import com.braintribe.build.artifact.walk.multi.clash.merger.DependencyMergerFactory;
import com.braintribe.build.artifact.walk.multi.exclusion.ExclusionControlFactory;
import com.braintribe.build.artifact.walk.multi.scope.ScopeControlFactory;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;

public interface WalkerFactory extends Function<WalkDenotationType, Walker> {
	void setMultiRepositoryDependencyResolverFactory( DependencyResolverFactory factory);
	void setSolutionEnricherFactory( MultiRepositorySolutionEnricherFactory factory);	
	void setExclusionControlFactory( ExclusionControlFactory factory);
	void setScopeControl( ScopeControlFactory factory);
	void setClashResolverFactory( ClashResolverFactory factory);
	void setDependencyMergerFactory( DependencyMergerFactory factory);
	void setPomExpertFactory( PomExpertFactory factory);	 
}
