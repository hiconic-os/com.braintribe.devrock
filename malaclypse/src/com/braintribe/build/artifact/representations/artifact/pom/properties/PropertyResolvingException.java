// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.properties;

public class PropertyResolvingException extends Exception {

	private static final long serialVersionUID = 1L;

	public PropertyResolvingException() {		
	}

	public PropertyResolvingException(String message) {
		super(message);
	}

	public PropertyResolvingException(Throwable cause) {
		super(cause);
	}

	public PropertyResolvingException(String message, Throwable cause) {
		super(message, cause);
	}

}
