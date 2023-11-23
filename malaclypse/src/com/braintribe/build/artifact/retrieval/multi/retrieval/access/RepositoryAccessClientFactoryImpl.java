// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.retrieval.access;

import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpRetrievalExpert;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;


public class RepositoryAccessClientFactoryImpl  implements RepositoryAccessClientFactory {
	
	private RepositoryAccessClient accessClient;
	private HttpAccess httpAccess;
	
	private HttpAccess getHttpAccess(boolean lenient) {
		if (httpAccess == null) {
			httpAccess = new HttpAccess( lenient);
		}
		return httpAccess;
	}
	
	@Override
	public RepositoryAccessClient apply(RavenhurstBundle bundle) throws RuntimeException {
		String index = bundle.getRepositoryClientKey();
		// standard, only the default client is supported 
		if (
				index == null || 
				index.equalsIgnoreCase(REPOSITORY_DEFAULT_CLIENT) || 
				index.equalsIgnoreCase(REPOSITORY_HTTP_CLIENT) || 
				index.equalsIgnoreCase(REPOSITORY_HTTPS_CLIENT)
			) {
			if (accessClient == null) {									
				accessClient = new HttpRetrievalExpert( getHttpAccess(bundle.getWeaklyCertified()));													
			}
			return accessClient;		 
		}
				
		if (index.equalsIgnoreCase( REPOSITORY_LOCAL_CLIENT) || index.equalsIgnoreCase( REPOSITORY_FILE_CLIENT)) {
			return new LocalRepositoryAccessClient();
		}
		String msg="no matching implementation of [" + RepositoryAccessClient.class.getName() + "] found for [" + index +"]";
		throw new RuntimeException(msg);		
	
	}

	@Override
	public void closeContext() {			
		if (httpAccess != null) {
			httpAccess.closeContext();
		}
	}
	
	
}
