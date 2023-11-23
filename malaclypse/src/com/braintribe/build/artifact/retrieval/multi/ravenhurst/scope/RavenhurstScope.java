// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry.RavenhurstPersistenceRegistry;

/**
 * combined interface for the ravenhurst scope 
 * @author Pit
 *
 */
public interface RavenhurstScope extends RavenhurstBundleProvider, LocalRepositoryProvider, RavenhurstMainDataHandler {
	void setReader(MavenSettingsReader reader);
	void setLockFactory( LockFactory lockFactory);
	RavenhurstPersistenceRegistry getPersistenceRegistry();
	void clear();
}
