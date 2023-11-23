// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt;

public interface Monitor {

	public void acknowledgeTask( String msg, int expectedSteps);
	public void acknowledgeStep( String msg, int i);	
	public void acknowledgeSubStep( String msg);
	public void acknowledgeModule( String module);
	public boolean isCancelled();
	public void done();
}
