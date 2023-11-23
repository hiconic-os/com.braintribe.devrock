// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom;

public class PomReaderException extends RuntimeException {

	private static final long serialVersionUID = 9090733677386522202L;

	public PomReaderException() {
		super();
	
	}

	public PomReaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public PomReaderException(String message) {
		super(message);	
	}

	public PomReaderException(Throwable cause) {
		super(cause);	
	}

}
