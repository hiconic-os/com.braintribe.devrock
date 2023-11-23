// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.retrieval.access.http;

public class HttpRetrievalException extends Exception {
	private static final long serialVersionUID = -2726682765372403779L;

	public HttpRetrievalException() {
	}

	public HttpRetrievalException(String arg0) {
		super(arg0);
	}

	public HttpRetrievalException(Throwable arg0) {
		super(arg0);		
	}

	public HttpRetrievalException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
