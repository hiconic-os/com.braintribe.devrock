// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence;

public class RepositoryPersistenceException extends RuntimeException {

	private static final long serialVersionUID = -1703327426776041367L;

	public RepositoryPersistenceException() {
	}

	public RepositoryPersistenceException(String message) {
		super(message);
	}

	public RepositoryPersistenceException(Throwable cause) {
		super(cause); 
	}

	public RepositoryPersistenceException(String message, Throwable cause) {
		super(message, cause);
	}


}
