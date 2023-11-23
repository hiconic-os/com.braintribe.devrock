// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.retrieval.access;

import java.util.function.Function;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactory;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

public interface RepositoryAccessClientFactory extends Function<RavenhurstBundle, RepositoryAccessClient> {
	static final String REPOSITORY_DEFAULT_CLIENT ="default";
	static final String REPOSITORY_HTTP_CLIENT ="http";
	static final String REPOSITORY_HTTPS_CLIENT ="https";
	static final String REPOSITORY_FILE_CLIENT ="file";
	static final String REPOSITORY_LOCAL_CLIENT = RepositoryInterrogationClientFactory.RAVENHURST_LOCAL_INTERROGATION_CLIENT;
	
	void closeContext();
}
