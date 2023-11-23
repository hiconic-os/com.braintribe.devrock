// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.clash;

public class ClashResolvingException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1691715459707076991L;

	public ClashResolvingException() {
	}


	public ClashResolvingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClashResolvingException(String message) {
		super(message);		
	}

	public ClashResolvingException(Throwable cause) {
		super(cause);
	}
	

}
