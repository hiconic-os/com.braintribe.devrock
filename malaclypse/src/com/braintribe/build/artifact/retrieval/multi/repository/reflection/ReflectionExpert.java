// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactory;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactory;

public interface ReflectionExpert {

	void setAccessClientFactory(RepositoryAccessClientFactory accessClientFactory);
	void setInterrogationClientFactory( RepositoryInterrogationClientFactory interrogationClientFactory);
	/**
	 * sets the parameterized current reader 
	 * @param reader - the {@link MavenSettingsReader} to use
	 */
	void setLocalRepositoryLocationProvider( LocalRepositoryLocationProvider localRepositoryLocationProvider);
}
