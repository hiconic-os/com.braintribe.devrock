// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.quickscan.standard;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.build.quickscan.QuickImportScanner;
import com.braintribe.build.quickscan.notification.QuickImportScanNotificationBroadcaster;
import com.braintribe.build.quickscan.notification.QuickImportScanNotificationListener;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;

public abstract class AbstractQuickImportScanner implements QuickImportScanner, QuickImportScanNotificationBroadcaster, QuickImportScanNotificationListener {
	private List<QuickImportScanNotificationListener> listeners = new ArrayList<QuickImportScanNotificationListener>();
	protected ProcessAbortSignaller scanAbortSignaller;
	protected SourceRepository sourceRepository;
	
	
	@Override @Configurable @Required
	public void setSourceRepository(SourceRepository sourceRepository) {
		this.sourceRepository = sourceRepository;
	}

	
	@Override
	public void acknowledgeScanned(SourceArtifact tuple) {
		for (QuickImportScanNotificationListener listener : listeners) {
			listener.acknowledgeScanned(tuple);
		}		
	}

	
	@Override
	public void acknowlegeScanError(String file) {
		for (QuickImportScanNotificationListener listener : listeners) {
			listener.acknowlegeScanError( file);
		}		
	}

	@Override
	public void addListener(QuickImportScanNotificationListener listener) {
		listeners.add(listener);		
	}

	@Override
	public void removeListener(QuickImportScanNotificationListener listener) {
		listeners.remove( listener);		
	}

	@Override @Configurable
	public void setScanAbortSignaller(ProcessAbortSignaller scanAbortSignaller) {
		this.scanAbortSignaller = scanAbortSignaller;
	}

}
