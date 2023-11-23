// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation;

public class RepositoryInterrogationException extends Exception {
	
	private static final long serialVersionUID = 1900753994492845110L;

	public RepositoryInterrogationException() {
	}

	public RepositoryInterrogationException(String message) {
		super(message);
	}

	public RepositoryInterrogationException(Throwable cause) {
		super(cause);
	}

	public RepositoryInterrogationException(String message, Throwable cause) {
		super(message, cause);
	}
}
