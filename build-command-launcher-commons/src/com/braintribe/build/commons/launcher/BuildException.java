// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.commons.launcher;

import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.utils.archives.ArchivesException;

public class BuildException extends Exception {

	public BuildException(String msg) {
		super(msg);
	}

	public BuildException(String msg, NameParserException e1) {
		super(msg, e1);
	}

	public BuildException(String msg, ArchivesException e) {
		super(msg, e);
	}

	private static final long serialVersionUID = 8053555611192693915L;

}
