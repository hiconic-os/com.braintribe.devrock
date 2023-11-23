// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.retrieval.access.http;

import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

public class HttpRetrievalCredentialsProvider implements CredentialsProvider {
	
	private Map<AuthScope, Credentials> authScopeToCredentialsMap;

	@Override
	public void setCredentials(AuthScope authscope, Credentials credentials) {
		authScopeToCredentialsMap.put(authscope, credentials);

	}

	@Override
	public Credentials getCredentials(AuthScope authscope) {
		return authScopeToCredentialsMap.get(authscope);
	}

	@Override
	public void clear() {
		authScopeToCredentialsMap.clear();
	}

}
