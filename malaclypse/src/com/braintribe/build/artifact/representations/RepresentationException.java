// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.artifact.representations;

/**
 * generic exception for all representations (in the future ;-))
 * @author pit
 *
 */
@SuppressWarnings("serial")
public class RepresentationException extends RuntimeException {

	public RepresentationException() {
	}

	public RepresentationException(String message) {
		super(message);
	}

	public RepresentationException(Throwable cause) {
		super(cause);
	}

	public RepresentationException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
