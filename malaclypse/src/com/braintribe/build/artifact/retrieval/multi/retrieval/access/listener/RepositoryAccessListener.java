// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.retrieval.access.listener;

public interface RepositoryAccessListener {

	void acknowledgeDownloadSuccess( String source, long millis);
	void acknowledgeDownloadFailure( String source, String reason);
	
	void acknowledgeUploadSuccess( String source, String target, long millis);
	void acknowledgeUploadFailure( String source, String target, String reason);
	
	void acknowledgeDeleteSuccess( String target, long millis);
	void acknowledgeDeleteFailure( String target, String reason);
	
}
