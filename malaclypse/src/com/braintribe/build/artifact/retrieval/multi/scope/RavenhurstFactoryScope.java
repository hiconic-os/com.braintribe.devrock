// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.scope;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;

/**
 * Ravenhurst related scope 
 * @author pit
 *
 */
public interface RavenhurstFactoryScope {
	/**
	 * returns a parametrized {@link RavenhurstScope}
	 */
	RavenhurstScope getRavenhurstScope();		
	/**
	 * returns a properly setup {@link RepositoryReflection}
	 */
	RepositoryReflection getRepositoryReflection();
	
	void release();
}
