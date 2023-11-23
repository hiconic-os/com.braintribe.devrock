// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation;

import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.logging.Logger;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;


public class RepositoryInterrogationClientFactoryImpl implements RepositoryInterrogationClientFactory {
	private static Logger log = Logger.getLogger(RepositoryInterrogationClientFactoryImpl.class);
	
	private RepositoryInterrogationClient interrogationClient; 
	private HttpAccess httpAccess;
	
	private Object initializationMonitor = new Object();
	
	private HttpAccess getHttpAccess() {
		if (httpAccess != null)
			return httpAccess;
		
		synchronized (initializationMonitor) {
			if (httpAccess != null)
				return httpAccess;
		
			httpAccess = new HttpAccess();			
		}		
		return httpAccess;
	}
	
	private Object clientInitializationMonitor = new Object();
	
	@Override
	public RepositoryInterrogationClient apply(RavenhurstBundle bundle) throws RuntimeException {
		String index = bundle.getRavenhurstClientKey();
		// standard, only the default client is supported 
		if (index == null || index.equalsIgnoreCase(RAVENHURST_DEFAULT_INTERROGATION_CLIENT) || 
							 index.equalsIgnoreCase(RAVENHURST_HTTP_CLIENT) || index.equalsIgnoreCase( RAVENHURST_HTTPS_CLIENT)
			) {
			if (interrogationClient != null)
				return interrogationClient;		 

			synchronized (clientInitializationMonitor) {
				if (interrogationClient != null)
					return interrogationClient;	
				interrogationClient = new RavenhurstInterrogationClient( getHttpAccess());
				return interrogationClient;
			}
		}
		if (index.equalsIgnoreCase( RAVENHURST_LOCAL_INTERROGATION_CLIENT) || index.equalsIgnoreCase( RAVENHURST_FILE_CLIENT)) {
			return new LocalRepositoryInterrogationClient();
		}
		String msg="no matching implementation of [" + RepositoryInterrogationClient.class.getName() + "] found for [" + index +"]";
		log.error( msg);
		throw new RuntimeException(msg);		
	}

	@Override
	public void closeContext() {
		if (httpAccess != null) {
			httpAccess.closeContext();
		}
	}
	
	

}
