// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt.testrun.client;

import java.awt.Button;

import com.braintribe.build.gwt.testrun.extra.X;

public class Failer {
	public static void main(String[] args) {
		Button button = new Button();
		X.foo();
	}
	
}
