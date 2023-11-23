// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi;

import java.util.Set;

import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.walk.multi.clash.ClashResolver;
import com.braintribe.build.artifact.walk.multi.exclusion.ExclusionControl;
import com.braintribe.build.artifact.walk.multi.listener.WalkNotificationListener;
import com.braintribe.build.artifact.walk.multi.scope.ScopeControl;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;

public interface ConfigurableWalker extends Walker {

	void setResolver( DependencyResolver resolver);
	void setEnricher( MultiRepositorySolutionEnricher enricher);
	void setClashResolver( ClashResolver resolver);
	void setPomExpertFactory( PomExpertFactory factory);
	void setExclusionControl( ExclusionControl control);
	void setScopeControl( ScopeControl control);
	void setAbortSignaller( ProcessAbortSignaller signaller);
	
	void setTagRule( String tagRule);
	void setTypeFilter( String typeFilter);
	void setAbortIfUnresolvedDependencyIsFound(boolean abortIfUnresolvedDependencyIsFound);
	
	void wireListeners();
	void unwireListeners(Set<WalkNotificationListener> listeners);
	void setWalkKind( WalkKind walkKind);

}
