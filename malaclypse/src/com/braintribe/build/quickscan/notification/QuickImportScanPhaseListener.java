// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.quickscan.notification;

import com.braintribe.model.panther.SourceArtifact;

public interface QuickImportScanPhaseListener {
	void acknowledgeEnumerationPhase();
	void acknowledgeScanPhase(int phase, int remaining);
	void acknowledgeDetected( String file);
	void acknowledgeScanError( String msg, String file);
	void acknowledgeUnresolved( int phases, String file);
	void acknowledgeResolved( SourceArtifact sourceArtifact);

}
