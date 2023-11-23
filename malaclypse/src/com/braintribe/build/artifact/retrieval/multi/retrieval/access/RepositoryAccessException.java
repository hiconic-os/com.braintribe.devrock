// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.retrieval.access;

public class RepositoryAccessException extends Exception{
	private static final long serialVersionUID = 8906507565143738078L;

	public RepositoryAccessException() {
		super();	
	}

	public RepositoryAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public RepositoryAccessException(String message) {
		super(message);
	}

	public RepositoryAccessException(Throwable cause) {
		super(cause);
	}


}
