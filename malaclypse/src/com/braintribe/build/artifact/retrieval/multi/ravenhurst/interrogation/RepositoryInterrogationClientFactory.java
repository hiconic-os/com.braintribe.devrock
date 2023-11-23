// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation;

import java.util.function.Function;

import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

public interface RepositoryInterrogationClientFactory extends Function<RavenhurstBundle, RepositoryInterrogationClient>{
	static final String RAVENHURST_DEFAULT_INTERROGATION_CLIENT ="default";
	static final String RAVENHURST_HTTP_CLIENT ="http";
	static final String RAVENHURST_HTTPS_CLIENT ="https";
	static final String RAVENHURST_FILE_CLIENT ="file";
	static final String RAVENHURST_LOCAL_INTERROGATION_CLIENT = "_localhost_";
	
	void closeContext();
}
