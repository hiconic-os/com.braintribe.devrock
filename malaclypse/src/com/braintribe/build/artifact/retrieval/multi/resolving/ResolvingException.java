// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;

public class ResolvingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResolvingException() {
	}

	public ResolvingException(String arg0) {
		super(arg0);
	}

	public ResolvingException(Throwable arg0) {
		super(arg0);
	}

	public ResolvingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}	
}
