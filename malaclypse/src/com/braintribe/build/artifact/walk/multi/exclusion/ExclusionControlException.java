// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.exclusion;

/**
 * @author Pit
 *
 */
public class ExclusionControlException extends RuntimeException{

	private static final long serialVersionUID = 3012849165545075538L;

	public ExclusionControlException() {
		super();
	}

	public ExclusionControlException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExclusionControlException(String message) {
		super(message);
	}

	public ExclusionControlException(Throwable cause) {
		super(cause);
	}


}
