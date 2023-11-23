// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.artifact.name;

@SuppressWarnings("serial")
public class NameParserException extends RuntimeException {

	public NameParserException() {
		super();		
	}

	public NameParserException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public NameParserException(String arg0) {
		super(arg0);
	}

	public NameParserException(Throwable arg0) {
		super(arg0);
	}

}
