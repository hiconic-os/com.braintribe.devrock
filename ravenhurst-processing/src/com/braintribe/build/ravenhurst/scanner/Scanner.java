// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.ravenhurst.scanner;

import java.util.Date;

public interface Scanner {

	public ChangedArtifacts getChangedArtifacts(String repository, Date timestamp) throws ScannerException;
	public Long getArtifactTimeStamp(String repository, String group, String artifact, String version) throws ScannerException;

}
