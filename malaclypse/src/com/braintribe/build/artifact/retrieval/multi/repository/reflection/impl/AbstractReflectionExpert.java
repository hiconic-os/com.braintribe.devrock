// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.ReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactory;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

public class AbstractReflectionExpert implements ReflectionExpert {	
	protected LocalRepositoryLocationProvider localRepositoryLocationProvider;
	protected RepositoryAccessClientFactory accessClientFactory;
	protected RepositoryInterrogationClientFactory interrogationClientFactory;
	
	
	@Override
	public void setAccessClientFactory(RepositoryAccessClientFactory accessClientFactory) {
		this.accessClientFactory = accessClientFactory;
	}

	@Override
	public void setInterrogationClientFactory(RepositoryInterrogationClientFactory interrogationClientFactory) {
		this.interrogationClientFactory = interrogationClientFactory;		
	}



	@Override @Configurable @Required
	public void setLocalRepositoryLocationProvider(LocalRepositoryLocationProvider localRepositoryLocationProvider) {
		this.localRepositoryLocationProvider = localRepositoryLocationProvider;
	}	
	
}
