// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.quickscan;

import java.util.List;

import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;

public interface QuickImportScanner {
	public void setSourceRepository(SourceRepository sourceRepository);
	public List<SourceArtifact> scanLocalWorkingCopy(String startLocation);
	public void setScanAbortSignaller(ProcessAbortSignaller scanAbortSignaller);
}
