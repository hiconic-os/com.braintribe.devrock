// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.build;

public class SolutionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SolutionException() {
		super();
	}

	public SolutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SolutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public SolutionException(String message) {
		super(message);
	}

	public SolutionException(Throwable cause) {
		super(cause);
	}

}
